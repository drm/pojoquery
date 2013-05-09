PojoQuery
=========

PojoQuery is a light-weight alternative to traditional Object Relational Mapping (ORM) frameworks in Java.
Instead of writing a SQL query in plain text, pojoQuery leverages plain Java classes to define the set of fields and tables (joins) we want to fetch.
Because each field or property in the POJO corresponds to a field in the SELECT clause of the query we can map the results to the same classes and obtain a type-safe set of results.

	@Table("user")
	class User {
		Long id;
		String firstName;
		String lastName;
		String email;
	}
	
	@Table("comment")
	class CommentDetail {
		Long id;
		String comment;
		Date submitdate;
		User author;
	}
	
	@Table("article")
	class Article
		Long id;
		String title;
		String content;
		Date publishdate;
	}
	
	class ArticleDetail {
		User author;
		CommentDetail[] comments;
	}
	
	class ArticleExample {
		DataSource database;
		
		ArticleDetail fetchArticle(Long articleId) {
			return PojoQuery.build(ArticleDetail.class)
					.addWhere("article.id=?", articleId)
					.addOrderBy("comments.submitdate DESC")
					.execute(database).get(0);
		}
	}

PojoQuery creates a SQL query from the `ArticleDetail` pojo, and transforms the JDBC ResultSet into `ArticleDetail` instances.
The exact SQL is easy to read and understand, much like you would write yourself:

	PojoQuery.build(ArticleDetail.class)
		.addWhere("article.id=?", articleId)
		.addOrderBy("comments.submitdate DESC")
		.toSql()	
	// returns:

	SELECT
	 `article`.id `article.id`,
	 `article`.title `article.title`,
	 `article`.content `article.content`,
	 `article`.author_id `article.author_id`,
	 `author`.id `author.id`,
	 `author`.firstName `author.firstName`,
	 `author`.lastName `author.lastName`,
	 `author`.email `author.email`,
	 `comments`.id `comments.id`,
	 `comments`.article_id `comments.article_id`,
	 `comments`.comment `comments.comment`,
	 `comments`.submitdate `comments.submitdate`,
	 `comments`.author_id `comments.author_id`,
	 `comments.author`.id `comments.author.id`,
	 `comments.author`.firstName `comments.author.firstName`,
	 `comments.author`.lastName `comments.author.lastName`,
	 `comments.author`.email `comments.author.email` 
	FROM article 
	 LEFT JOIN user `author` ON `article`.author_id=`author`.id
	 LEFT JOIN comment `comments` ON `comments`.article_id=`article`.id
	 LEFT JOIN user `comments.author` ON `comments`.author_id=`comments.author`.id 
	WHERE article.id=?  
	ORDER BY comments.submitdate DESC 

Note that PojoQuery 'guesses' names of linkfields using the default strategy [tablename]_id. (You can use annotations to override field and table names at any time).

The major difference with JPA is that instead of POJO's defining ALL links in the database we only specify the links to FETCH.
This means that there is no lazy loading.

Although lazy loading has its obvious benefits (i.e. no need to specify which linked entities to load beforehand), the drawbacks are significant: 
all business logic must be contained in a session
we cannot serialize classes easily
generating proxy classes that kill instanceof and easy debugging
the implementing framework needs to handle a lot of complexity

The alternative that PojoQuery offers is best to think of as a 'view': a set of fields and tables with their respective join conditions.
Thought of it this way, ArticleDetail is a 'view' that contains all information needed to display an article in a blog: the title, content, comments and their respective authors.
When displaying artiles in a list, we would create a different view, because we are not interested in the comments, we only need to show an author. Easy enough:

	class ArticleListView extends Article {
		User author;
	}

You still have full control over the SQL that is generated:
Let's say we want to improve on the list by adding the number of comments and date of last comment. We can add custom query clauses using annotations.

	@Join("LEFT JOIN comment ON comment.article_id=article.id")
	@GroupBy("comment.id")
	class ArticleListView extends Article {
		Author author;
		
		@Select("COUNT(comment.id)")
		int commentCount;
		
		@Select("MAX(comment.submitDate)")
		Date lastCommentDate;
	}

The Join, GroupBy and Select clauses are simply copied into the query.

