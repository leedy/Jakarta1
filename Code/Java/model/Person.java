package model;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

//Finally, in the latest version, you can also do @DocumentConfig(formName="SomeThirdName") to override both. 
//That's useful in the case where you have entity classes representing the same form name in multiple DBs, since 
//NoSQL entity names must be unique
// @DocumentConfig(formName="SomeThirdName")



@Entity("fUserName")  // normal formname use
public class Person {
	@Id
	private String unid;
	
	@Column("number")
	private String key;
	
	@Column("FirstName")
	private String firstName;
	
	@Column("LastName")
	private String lastName;
	
	@Column("State")
	private String state;

	public String getUnid() { return unid; }
	public void setUnid(String unid) { this.unid = unid; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	
	public String getState() { return state; }
	public void setState(String state) { this.state = state; }
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
