package util
{
	
	import mx.collections.ArrayCollection;
	
	/** This class represents a map interface. */
	public interface UMap
	{
		
        /** Removes all key/value pair mappings from this hashmap. */
        function clear():void 
  
        /** 
		 * Delivers the value for the given key. 
		 * 
		 * @param key the key whose mapped value should be returned.
		 * @return the value object that is mapped to the given key.*/
        function getValue(key:*):Object 

		/**
		 * Adds the given key/value pair to the hashmap.
		 * <p>
		 * It is checked if the given key was already in the hashmap. In this case, 
		 * the method overrides the existing mapping with the new value and returns true 
		 * as an indication of pre-existence of the key. If the given key hasn`t existed so far, 
		 * the method adds the mapping, increases the <i> size-counter </i> and returns false.
		 * 
		 * @param key the key to add.
		 * @param value the value to add.
		 * @return true, if the key already existed and false otherwise.
		 * */
        function put(key:*, value:Object):Boolean 
        
        /**
         * Adds all key/value pairs of the given hashmap to this hashmap.
         * 
         * @param otherMap the specified hashmap whose key/value pairs should be added.
         * */
        function putAll(otherMap:UMap):void 

		/**
		 * Removes the key and its mapped value from the hashmap.
		 * 
		 * @param key the key that should be removed from the hashmap.
		 * @return the mapped value, if the key could be removed and null otherwise.
		 * */
        function remove(key:*):Object 
        
        /**
         * Removes all entries of the given hashmap from this hashmap.
         * 
         * @param otherMap the specified hashmap whose entires should be removed from this hashmap.
         * */
        function removeAll(otherMap:UMap):void 
        
        /** 
		 * Checks if the specified key is already in the hashmap (and mapped to a value). 
		 * 
		 * @param key the key that should be checked for existence in the hashmap.
		 * @return true, if the hashmap contains the given key and false otherwise.
		 * */
        function containsKey(key:*):Boolean 
        
        /** 
		 * Checks if this hashmap contains all keys of the specified hashmap. 
		 * 
		 * @param otherMap the other hashmap whose keys should be contained in this hashmap.
		 * @return true, if this hashmap contains all keys of the specified hashmap and false otherwise.
		 * */
        function containsAllKeys(otherMap:UMap):Boolean 
          
        /** 
		 * Checks if the specified value is already in the hashmap (and mapped to a key). 
		 * <p>
		 * This method iterates over all keys in the hashmap and determines if the specified 
		 * value is mapped to one of the found keys.
		 * 
		 * @param value the value that should be checked for existence in the hashmap.
		 * @return true, if the hashmap contains the given value and false otherwise.
		 * */
        function containsValue(value:Object):Boolean 
          
		/**
         * Delivers the size of (number of key/value pairs in) the hashmap.
         * <p>
         * It is necessary to compute the size at least once (though the methods
         * that add and remove entries adjust the size counter.) The size computation 
         * is necessary, because this object could be a serialized object which originates 
         * from a java hashmap. In this case, we have no <i> size </i> data stored already and so
         * we will have to compute this data once. 
         * 
         * @return the number of key/value pairs.
         * */
        function size():int 

		/**
		 * Delivers a string object consisting of the internally stored key/value pairs.
		 * 
		 * @return the string representation of the hashmap.
		 * */
        function toString():String 

		/**
		 * Delivers an array representation of the internally stored key/value pairs.
		 * 
		 * @return the array representation.
		 * */
        function values():ArrayCollection 
        
		/**
		 * Checks whether this hashmap is empty or not.
		 * 
		 * @return true, if this hashmap is empty and false otherwise.
		 * */
        function isEmpty():Boolean 
        
		/** 
		 * Delivers an object (set) that stores all the keys of the hashmap. 
		 * 
		 * @return the keys of the hashmap stored in a set.
		 * */
        function keySet():Object 
		
		 /**
         * Checks whether both hashmaps (this instance and the specified hashmap) contain the same 
         * entries or not. 
         * 
         * @param orherMap the specified hashmap which should be tested for equality.
         * @return true, if both maps are equal and false otherwise.
         * */
        function equals(otherMap:UMap):Boolean 
	}
}