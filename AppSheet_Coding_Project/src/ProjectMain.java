import java.util.*;
import java.net.*;
import java.io.*;

public class ProjectMain 
{
	// Relevant URL's and extensions
	public static final String URL_BASE = 
			"https://appsheettest1.azurewebsites.net/sample/";
	
	public static final String INITIAL_LIST_EXTENSION = "list";
	public static final String LIST_EXTENSION = "list?token=";
	public static final String PERSON_EXTENSION = "detail/";
	public static final String DUMMY_EXTENSION = "";
	
	// Schema for all queries
	// {"name1": data1,"name2": data2, ...}
	public static final String OUTER_DELIM = ",";
	public static final String INNER_DELIM = ":";
	public static final String START_DELIM = "{";
	public static final String END_DELIM = "}";
	public static final int DATA_INDEX = 1;
	
	// Schema for "list" queries
	// {"result":[1,2,3,4,5,6,7,8,9,10],"token":"a35b4"}
	public static final String LIST_DELIM = ",";
	public static final String LIST_START_DELIM = "[";
	public static final String LIST_END_DELIM = "]";
	public static final String TOKEN_STRING = "\"token\"";
	
	// Schema for "person" queries
	// {"id":21,"name":"paul","age":48,"number":"555-555-5555"}
	public static final int ID_INDEX = 0;
	public static final int NAME_INDEX = 1;
	public static final int AGE_INDEX = 2;
	public static final int NUMBER_INDEX = 3;
	
	public static final int RESULT_SIZE = 5;
	
	public static void main(String[] args) throws IOException 
	{
		List<Integer> ids = getIDs();
		List<Person> people = getPeople(ids);
		
		PriorityQueue<Person> result = new PriorityQueue<Person>();
		for (int i = 0; i < people.size(); i++)
		{
			Person person = people.get(i);
			if (isNumberValid(person.phoneNumber))
			{
				if (result.size() < RESULT_SIZE)
				{
					result.add(person);
				}
				else
				{
					if (person.age < result.peek().age)
					{
						result.remove();
						result.add(person);
					}
				}
			}
		}
		
		///
		// Print the results
		///
		
		Iterator<Person> itPrint = result.iterator();
		while (itPrint.hasNext())
		{
			System.out.println(itPrint.next());
		}
		
		///
		// Verify the results
		///
		
		boolean verified = true;
		
		// Do all the results have valid phone numbers?
		Iterator<Person> itTestNumber = result.iterator();
		while (itTestNumber.hasNext())
		{
			Person resultPerson = itTestNumber.next();
			verified = verified && isNumberValid(resultPerson.phoneNumber);
		}
		
		// Are the results the youngest people with valid phone numbers?
		for (int i = 0; i < people.size(); i++)
		{
			Person person = people.get(i);
			if (!result.contains(person) && isNumberValid(person.phoneNumber))
			{
				Iterator<Person> itTestAge = result.iterator();
				
				while (itTestAge.hasNext())
				{
					Person resultPerson = itTestAge.next();
					verified = verified && (person.age >= resultPerson.age);
				}
			}
		}
		
		System.out.println("\nVerified?: " + verified);
	}
	
	// Out: Queries the test service and returns a list of the ids
	// of all the dummy people in the various webpages.
	public static List<Integer> getIDs() throws IOException
	{
		List<Integer> ids = new ArrayList<Integer>();
		String currentExtension = INITIAL_LIST_EXTENSION;
		
		while (currentExtension != DUMMY_EXTENSION)
		{
			URL current = new URL(URL_BASE + currentExtension);
			
	        BufferedReader in = new BufferedReader(new InputStreamReader(current.openStream()));
	        String idsLine = in.readLine();
	        in.close();
	        
	        int tokenIndex = idsLine.indexOf(OUTER_DELIM + TOKEN_STRING);
	        int splitIndex;
	        if (tokenIndex > 0)
	        {
	        	splitIndex = tokenIndex;
	        }
	        else
	        {
	        	splitIndex = idsLine.length();
	        }
	        
	        String idsStrsRaw = idsLine
	        		.substring(0, splitIndex)
	        		.split(INNER_DELIM)[DATA_INDEX];
	        
	        String[] idsStrs = idsStrsRaw
	        		.replace(LIST_START_DELIM,  "")
	        		.replace(LIST_END_DELIM, "")
	        		.replace(END_DELIM, "")
	        		.split(LIST_DELIM);
	        
	        for (int i = 0; i < idsStrs.length; i++)
	        {
	        	ids.add(Integer.parseInt(idsStrs[i]));
	        }
	        
	        // Token index is -1 if indexOf("token") failed
	        if (tokenIndex > 0)
	        {
	        	String tokenRaw = idsLine
	        			.substring(tokenIndex, idsLine.length())
	        			.split(INNER_DELIM)[DATA_INDEX];

	        	currentExtension =
	        			LIST_EXTENSION + 
	        			tokenRaw.replace(END_DELIM, "").replace("\"", "");
	        }
	        else
	        {
	        	currentExtension = DUMMY_EXTENSION;
	        }
		}
		
		return ids;
	}
	
	// In: Takes in a list  of integers indicating the ids of the 
	// dummy people.
	// Out: Queries the test service and constructs and returns a list
	// of Person objects for each of the person ids in the input list.
	public static List<Person> getPeople(List<Integer> ids) throws IOException
	{
		List<Person> people = new ArrayList<Person>();
		
		for (int i = 0; i < ids.size(); i++)
		{
			int id = ids.get(i);
			
			URL current = new URL(URL_BASE + PERSON_EXTENSION + id);
			
	        BufferedReader in = new BufferedReader(new InputStreamReader(current.openStream()));
	        String personLine = in.readLine();
	        in.close();
	        
	        String[] personLineSplit = personLine.split(OUTER_DELIM);
	        
	        String name = personLineSplit[NAME_INDEX]
	        		.split(INNER_DELIM)[DATA_INDEX].replace("\"", "");
	        
	        String ageStr = personLineSplit[AGE_INDEX]
    				.split(INNER_DELIM)[DATA_INDEX].replace("\"", "");
	        
	        String phoneNumber = personLineSplit[NUMBER_INDEX]
	        		.split(INNER_DELIM)[DATA_INDEX].replace("\"", "").replace(END_DELIM, "");
	        
	        people.add(new Person(name, Integer.parseInt(ageStr), phoneNumber));
		}
		
		return people;
	}
	

	// In: Takes in a String representing a phone number
	// Out: Returns a boolean indicating whether the phone number is a valid
	// US phone number. For the purposes of this project, a valid US phone 
	// number has ten digits.
	public static boolean isNumberValid(String number)
	{
		int numberCount = 0;
		for (int i = 0; i < number.length(); i++)
		{
			if (Character.isDigit(number.charAt(i)))
			{
				numberCount++;
			}
		}
		
		return numberCount == 10;
	}
}
