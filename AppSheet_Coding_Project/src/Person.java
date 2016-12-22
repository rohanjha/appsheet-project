public class Person implements Comparable<Person>
{
	String name;
	int age;
	String phoneNumber;
	
	public Person(String name, int age, String phoneNumber)
	{
		this.name = name;
		this.age = age;
		this.phoneNumber = phoneNumber;
	}
	
	public int compareTo(Person other)
	{
		return other.age - this.age;
	}
	
	public String toString()
	{
		return this.name + ", " + this.age + ", " + this.phoneNumber;
	}
}
