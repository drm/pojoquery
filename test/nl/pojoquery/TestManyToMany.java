package nl.pojoquery;

import static nl.pojoquery.TestUtils.norm;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import nl.pojoquery.annotations.Id;
import nl.pojoquery.annotations.Link;
import nl.pojoquery.annotations.Table;

public class TestManyToMany {

	@Table("person")
	static class PersonRecord {
		@Id
		Long id;
		int age;
		String firstname;
		String lastname;
	}
	
	static class Person extends PersonRecord {
		List<EmailAddress> emailAddresses;
	}
	
	@Table("emailaddress")
	static class EmailAddress {
		Long person_id;
		String email;
		String name;
	}


	@Table("event")
	static class Event {
		@Id
		Long id;
		String title;
		Date date;
	}
	
	public static class EventWithPersons extends Event {
		@Link(linktable="event_person")
		public List<Person> persons;

	}
	
	public static class PersonWithEvents extends Person {
		@Link(linktable="event_person")
		private List<Event> events;
		
		public List<Event> getEvents() {
			return events;
		}

		public void setEvents(List<Event> events) {
			this.events = events;
		}
	}
	
	@Table("event_person")
	public static class EventPersonLink {
		public Person person;
		public Event event;
	}
	
	
	@Test
	public void testWhere() {
		PojoQuery<EventWithPersons> q = PojoQuery.build(EventWithPersons.class)
				.addWhere("persons.firstname=?", "John");
		
		Assert.assertEquals(
			norm(
				"SELECT\n" + 
				" `event`.id AS `event.id`,\n" + 
				" `event`.title AS `event.title`,\n" + 
				" `event`.date AS `event.date`,\n" + 
				" `persons`.id AS `persons.id`,\n" + 
				" `persons`.age AS `persons.age`,\n" + 
				" `persons`.firstname AS `persons.firstname`,\n" + 
				" `persons`.lastname AS `persons.lastname`,\n" + 
				" `persons.emailAddresses`.person_id AS `persons.emailAddresses.person_id`,\n" + 
				" `persons.emailAddresses`.email AS `persons.emailAddresses.email`,\n" + 
				" `persons.emailAddresses`.name AS `persons.emailAddresses.name`\n" + 
				"FROM event\n" + 
				" LEFT JOIN event_person AS `event_person` ON `event`.id = `event_person`.event_id\n" + 
				" LEFT JOIN person AS `persons` ON `event_person`.person_id = `persons`.id\n" + 
				" LEFT JOIN emailaddress AS `persons.emailAddresses` ON `persons`.id = `persons.emailAddresses`.person_id\n" + 
				"WHERE persons.firstname=?"), 
			norm(q.toSql()));
	}

}
