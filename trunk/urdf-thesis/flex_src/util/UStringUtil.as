package util
{
	
	import mx.utils.StringUtil;
	import flare.util.Arrays;
	
	/**
	 * This class represents an utility class to deal with common String functionality. 
	 * <p>
	 * The class acts as a function library.
	 * 
	 * */
	public class UStringUtil extends mx.utils.StringUtil
	{
		
		/**
		* Compares two given strings and returns true if
		* they are equal.
		* 
		* @param str1 The first string to compare.
		* @param str2 The second string to compare.
		*
		* @return true, if both string object represent the same string, 
		*        false otherwise.	
		*/			
		public static function equalsIgnoreCase(str1:String, str2:String):Boolean
		{ 
			return (str1 == str2);	
		}
		
		/**
		* Compares two given strings and returns true if
		* they are equal.
		* 
		* @param str1 The first string to compare.
		* @param str2 The second string to compare.
		*
		* @return true, if both string object represent the same string, 
		*        false otherwise.	
		*/			
		public static function equals(str1:String, str2:String, caseSensitive:Boolean):Boolean
		{
			if(caseSensitive)
			{
				return (str1 == str2);
			}
			else
			{
				return (str1.toLowerCase() == str2.toLowerCase());
			}
		}
 
 
        /**
	     * Checks if the given string (String object) is empty ("") or null.
	     *
	     * StringUtil.isEmpty(null)      = true
	     * StringUtil.isEmpty("")        = true
	     * StringUtil.isEmpty(" ")       = false
	     * StringUtil.isEmpty("bob")     = false
	     * StringUtil.isEmpty("  bob  ") = false
	     *
	     * @param str 
	     *          the string tp check.
	     * @return true , if the given string is empty or null.
	     */
	    public static function isEmpty(str:String):Boolean
	    {
	    	if(str == null || str.length == 0)
	    		return true;
	    	else
	    		return false;
	    }
 
		
		/**
		*	Removes whitespace characters in front and at the end of the given string.
		* 
		*	@param str the input string from which to remove the whitespace characters at the beginning
		*              and at the end.
		*	will be removed.
		*
		*	@return the trimmed string (removed whitespace at the beginnning and at the end).
		*/			
		/*public static function trim(str:String):String
		{
			return UStringUtil.ltrim(UStringUtil.rtrim(str));
		}*/

		/**
		*	Removes whitespace from the front of the specified string.
		* 
		*	@param input The String whose beginning whitespace will will be removed.
		*
		*	@return A String with whitespace removed from the begining	
		*
		*/	
		/*public static function ltrim(input:String):String
		{
			var size:Number = input.length;
			for(var i:Number = 0; i < size; i++)
			{
				if(input.charCodeAt(i) > 32)
				{
					return input.substring(i);
				}
			}
			return "";
		}*/

		/**
		*	Removes whitespace from the end of the specified string.
		* 
		*	@param input The String whose ending whitespace will will be removed.
		*
		*	@return A String with whitespace removed from the end	
		*
		*/	
		/*public static function rtrim(input:String):String
		{
			var size:Number = input.length;
			for(var i:Number = size; i > 0; i--)
			{
				if(input.charCodeAt(i - 1) > 32)
				{
					return input.substring(0, i);
				}
			}

			return "";
		}*/

		/**
		*	Checks whether the given string begins with the specified prefix or not.
		* 
		*	@param str the string that is checked to contain the given prefix at the beginning.
		*	@param prefix the prefix that is checked to be contained in the given string.
		*
		*	@return true, if the given string contains the specified prefix at the beginning, 
		*            false otherwise.
		*/	
		public static function startsWith(str:String, prefix:String):Boolean
		{	
			if(str == null || prefix == null)
			   return false;
			if(str == "" && prefix == "")
			   return true;	
			return (prefix == str.substring(0, prefix.length));
		}	

		/**
		*	Checks whether the given string ends with the specified suffix, or not.
		* 
		*	@param str the string that is checked to contain the given suffix at the end.
		*	@param suffix the suffix.
		*
		*	@return True if the string ends with the suffix, false if it does not.
		*/	
		public static function endsWith(str:String, suffix:String):Boolean
		{
			return (suffix == str.substring(str.length - suffix.length));
		}	

		/**
		*	Removes all occurrences of the <i> expression </i> string from the given string.
		* 
		*	@param str the string from which to remove all occurrences of the specified
		*             expressiion string.
		*	@param expression the expression string whose occurrences should be removed.
		*	@return a new string where all the substrings containing the expression-string 
		*          are removed.
		*/	
		public static function remove(str:String, expression:String):String
		{
			return UStringUtil.replace(str, expression, "");
		}
		
		/**
		*	Replaces all occurrences of the .
		* 
		*	@param input The string that instances of replace string will be 
		*	replaces with removeWith string.
		*
		*	@param replace The string that will be replaced by instances of 
		*	the replaceWith string.
		*
		*	@param replaceWith The string that will replace instances of replace
		*	string.
		*
		*	@return A new String with the replace string replaced with the 
		*	replaceWith string.
		*
		*/
		public static function replace(input:String, replace:String, replaceWith:String):String
		{
			return input.split(replace).join(replaceWith);
		} 
		
		
		/**
		*	Checks whether the given string is either not null or contains
		*  	any character.
		* 
		*	@param str the string to be checked for a value.
		*   @return true, if the string has a value, false otherwise.
		*/		
		public static function hasValue(str:String):Boolean
		{
			//todo: this needs a unit test
			return (str != null && str.length > 0);			
		}

		/**
		 * Compares two given strings (String objects), hereby ignoring the cases of the strings.
		 * <p>
		 * There are several cases (possible return values):
		 * <p>
		 * return value is <i> -1 </i>: 
		 *     if <i> str1 </i> is <i> null </i> and <i> str2 </i> is not or <i> str1 </i> is lexically smaller than <i> str2 </i> 
		 * <p>
		 * return value is <i> 0 </i>: 
		 *     if <i> str1 </i> and <i> str2 </i> are <i> null </i> or str1 and str2 represent the same sequence of chars.
		 * <p>
		 * return value is <i> 1 </i>: 
		 *     if <i> str2 </i> is <i> null </i> and <i> str1 </i> is not or <i> str1 </i> is lexically bigger than <i> str2 </i>
		 * 
		 * @param str1 the first string to check for equality (the left operand).
		 * @param str2 the second string to check for equality (the right operand).
		 * @return the numeric vale which indicates the case of the comparison result. 
		 * */
		public static function compareToIgnoreCase(str1:String, str2:String):int {
			
			// str1 = null
			if(str1 == null) {
				
				// str1 = str2 = null
				if(str2 == null)
				   return 0;
				// str1 = null and str2 has a value
				else 
				   return -1;	
			}
			
			// str1 != null
			else {
				
				// str1 != null and str2 = null 
				if(str2 == null)
				   return 1;
				// str1 != null and str2 != null
				else {
				   
				   // str1 < str2
				   if(str1 < str2)
				      return -1;
				   // str1 = str2
				   if(str1 == str2)
				      return 0;
				   // str1 > str2
				   else
				      return 1;
				}
				
			}
			
		} 
		
		
		/**
		 * Compares two given strings (String objects), hereby transforming the given strings 
		 * into lower-case.
		 * <p>
		 * There are several cases (possible return values):
		 * <p>
		 * return value is <i> -1 </i>: 
		 *     if <i> str1 </i> is <i> null </i> and <i> str2 </i> is not or <i> str1 </i> is lexically smaller than <i> str2 </i> 
		 * <p>
		 * return value is <i> 0 </i>: 
		 *     if <i> str1 </i> and <i> str2 </i> are <i> null </i> or str1 and str2 represent the same sequence of chars.
		 * <p>
		 * return value is <i> 1 </i>: 
		 *     if <i> str2 </i> is <i> null </i> and <i> str1 </i> is not or <i> str1 </i> is lexically bigger than <i> str2 </i>
		 * 
		 * @param str1 the first string to check for equality (the left operand).
		 * @param str2 the second string to check for equality (the right operand).
		 * @return the numeric vale which indicates the case of the comparison result. 
		 * */
		public static function compareTo(str1:String, str2:String):int {
				
			
			// str1 = null
			if(str1 == null) {
				
				// str1 = str2 = null
				if(str2 == null)
				   return 0;
				// str1 = null and str2 has a value
				else 
				   return -1;	
			}
			
			// str1 != null
			else {
				
				// str1 != null and str2 = null 
				if(str2 == null)
				   return 1;
				// str1 != null and str2 != null
				else {
				   
				   // str1 < str2
				   if(str1.toLowerCase() < str2.toLowerCase())
				      return -1;
				   // str1 = str2
				   if(str1.toLowerCase() == str2.toLowerCase())
				      return 0;
				   // str1 > str2
				   else
				      return 1;
				}
				
			}
			
		}
		
		/**
	     * Returns a hash code for this string. The hash code for a
	     * <code>String</code> object is computed as
	     * <blockquote><pre>
	     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
	     * </pre></blockquote>
	     * using <code>int</code> arithmetic, where <code>s[i]</code> is the
	     * <i>i</i>th character of the string, <code>n</code> is the length of
	     * the string, and <code>^</code> indicates exponentiation.
	     * (The hash value of the empty string is zero.)
	     *
	     * @return  a hash code value for this object.
	     */
	    public static function hashCode(str:String):int {
		   
		  /* var hash:int = 0;
		   var off:int = 0;
		   var chars:Array = str.split("");
		   var len:int = chars.length;
	
	       for (var i:int = 0; i < len; i++) {
	          hash = 31*hash + chars[off++];
		   }
				
	       return hash;
	       */
	       var hash:int = 0;
		   var len:int = str.length;
		   for (var i:int = 0; i<len; i++)
		   {
			   hash += (i + 1) * str.charCodeAt(i);
		   }
		   return hash;
	    }
	    
	    /**
	    * Sorts the given array of Strings in a a lexicographical style, thereby using
	    * the specified order.
	    * */
	   
	    public static function sortLexicographical(sortList:Array):Array {
	    	
	    	var tempList:Array = new Array();
	    	var paramToSort:String;
	    	//var sortedParam:String;
	    	
	    	var i:int = 0;
	    	//var j:int = 0;
	    	
	    	// we have to check that we only have String instances as parameters
	    	for (i; i<sortList.length; i++) {
	    		// check if the object is a string
	    		// if not, we cannot aply our sorting -> abort the procedure
	    		if(!(sortList[i] is String))
	    		   return null;
	    	}
	    	
	    	//i = 0;
	    	var idx:int = 0;
	    	
	    	for each (paramToSort in sortList) {
	    		idx = Arrays.binarySearch(tempList, paramToSort);//_sort.comparator);
			    tempList.splice(-(idx+1), 0, paramToSort);
	    	}
	    	
	    	return tempList;
	    	
	    	/*
	    	for (i; i<sortList.length; i++) {
	    		for (j; j<tempList.length; j++) {
	    			
	    			paramToSort = sortList[i];
	    			sortedParam = tempList[j];
	    			
	    			switch(compareToIgnoreCase(paramToSort, sortedParam)) {
	    				// the param to sort is smaller
	    				case -1: 
	    				// the param to sort is equal
	    				case -1: 
	    				// the param to sort is bigger
	    				case -1: 
	    			}
	    		}
	    	}*/
	    	
	    }
	    
	    
	    
	    /**
		 * Performs a binary search over the input array for the given key
		 * value, optionally using a provided property to extract from array
		 * items and a custom comparison function.
		 * @param a the array to search over
		 * @param key the key value to search for
		 * @return the index of the given key if it exists in the array,
         *  otherwise -1 times the index value at the insertion point that
         *  would be used if the key were added to the array.
         */
		public static function binarySearch(a:Array, key:String):int //, cmp:Function=null) : int
		{
			//if (cmp == null)
				//cmp = function(a:*,b:*):int {return a>b ? 1 : a<b ? -1 : 0;}
			
			// we use our compareToIgnoreCase(str1:String, str2:String) function
			
			var x1:int = 0, x2:int = a.length, i:int = (x2>>1);
        	while (x1 < x2) {
        		//var c:int = cmp(a[i] as String, key);
        		var c:int = compareTo(a[i] as String, key);
        		if (c == 0) {
                	return i;
            	} else if (c < 0) {
                	x1 = i + 1;
            	} else {
                	x2 = i;
            	}
            	i = x1 + ((x2 - x1)>>1);
        	}
        	return -1*(i+1);
		}
		
		public static  function str_replace(stringToReplace:String, replaceString:String, originalString:String ):String
		{
			var array:Array = originalString.split(replaceString);
			return array.join(stringToReplace);
		}
	  

	}
}