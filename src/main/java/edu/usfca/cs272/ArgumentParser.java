package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Parses and stores command-line arguments into simple flag/value pairs.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022 
 */
public class ArgumentParser {
	/**
	 * Stores command-line arguments in flag/value pairs.
	 */
	private final HashMap<String, String> map;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentParser() {
		this.map = new HashMap<>();
	}

	/**
	 * Initializes this argument map and then parsers the arguments into
	 * flag/value pairs where possible. Some flags may not have associated values.
	 * If a flag is repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentParser(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Determines whether the argument is a flag. The argument is considered a
	 * flag if it is a dash "-" character followed by any character that is not a
	 * digit or whitespace. For example, "-hello" and "-@world" are considered
	 * flags, but "-10" and "- hello" are not.
	 *
	 * @param arg the argument to test if it's a flag
	 * @return {@code true} if the argument is a flag
	 *
	 * @see String#startsWith(String)
	 * @see String#length()
	 * @see String#codePointAt(int)
	 * @see Character#isDigit(int)
	 * @see Character#isWhitespace(int)
	 */
	public static boolean isFlag(String arg) {
		/* TODO 
		if (arg == null || arg.length() < 2 || !arg.startsWith("-")){
			return false;
		}
		
		int second = arg.codePointAt(1);
		return !(Character.isDigit(second) || Character.isWhitespace(second));
		*/
		
		if (arg == null || arg.length() < 2){
			return false;
		}
		if (!arg.startsWith("-")){
			return false; //doesn't start with "-"
		}
		char c = arg.charAt(1);
		if (Character.isDigit(c) || Character.isWhitespace(c)){
			return false;
		}
		return true;
	}

	/**
	 * Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg) {
		return !isFlag(arg);
	}

	/**
	 * Puts a new key value pair in the map.
	 * If the key exists, it's value will be replaced.
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag to be added as a key.
	 * @param value the value to be added as a value.
	 */
	public void storeInMap(String flag, String value) { // TODO Remove
		if (value == "null"){
			value = null;
		}
		if (hasFlag(flag))
			map.replace(flag,value);
		else
			map.put(flag,value);
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may
	 * not have associated values. If a flag is repeated, its value will be
	 * overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public void parse(String[] args) {
		for(int i = 0; i < args.length; i++){
			if (isFlag(args[i])){
				if (args.length > i+1 && isValue(args[i+1])){
					storeInMap(args[i],args[i+1]); // TODO map.put
				} else {
					storeInMap(args[i],"null"); // TODO map.put(args[i], null)
				}
			}
		}
	}

	/**
	 * Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags() {
		return map.keySet().size();
	}

	/**
	 * Determines whether the specified flag exists.
	 *
	 * @param flag the flag check
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag) {
		return map.containsKey(flag);
	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag) {
		return (map.get(flag) != null);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or the backup value if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @param backup the backup value to return if there is no mapping
	 * @return the value to which the specified flag is mapped, or the backup
	 *   value if there is no mapping
	 */
	public String getString(String flag, String backup) {
		String value = map.get(flag);
		if (value != null){
			return value;
		} else
			return backup;

	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped or {@code null} if
	 *   there is no mapping
	 */
	public String getString(String flag) {
		return map.get(flag);
	}

	/**
	 * Returns the value the specified flag is mapped as a {@link Path}, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *   backup value if there is no valid mapping
	 *
	 * @see Path#of(String, String...)
	 */
	public Path getPath(String flag, Path backup) {
		try {
			return Path.of(map.get(flag));
		} catch (Exception e) {
			return backup;
		}
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link Path},
	 * or {@code null} if unable to retrieve this mapping (including being unable
	 * to convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *   unable to retrieve this mapping
	 *
	 * @see #getPath(String, Path)
	 */
	public Path getPath(String flag) {
		try {
			return Path.of(map.get(flag));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as an int, or the backup
	 *   value if there is no valid mapping
	 *
	 * @see Integer#parseInt(String)
	 */
	public int getInteger(String flag, int backup) {
		try {
			return Integer.parseInt(map.get(flag));
		} catch (NumberFormatException e){
			return backup;
		}
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or 0 if
	 * unable to retrieve this mapping (including being unable to convert the
	 * value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @return the value the specified flag is mapped as an int, or 0 if there is
	 *   no valid mapping
	 *
	 * @see #getInteger(String, int)
	 */
	public int getInteger(String flag) {
		try {
			return Integer.parseInt(map.get(flag));
		} catch (NumberFormatException e){
			return 0;
		}
	}

	@Override
	public String toString() {
		return this.map.toString();
	}
}
