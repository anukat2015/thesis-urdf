package util
{	 
	
	import mx.collections.ArrayCollection;
	
	/**
	 * This class represents a hashmap implementation which is based on the Object super class and uses 
     * strings (String objects) as keys.
     * <p>
     * This data structure stores mappings between key/value pairs. Strings (String objects) are used as keys
     *  and as values objects of any kind. Methods roughly mirror those of the java HashMap class. 
	 * 
	 * @author Timm Meiser
	 * @since 11.01.10
	 * @version 1.0
	 *
	 */ 
    public class UStringHashMap {//implements UMap {

        private var map:Object;

		/** The size-counter for the hashmap. */
		private var length:int = 0; 
		/** The flag that states if the <i> size() </i>-method has been called at lteast once. */
        private var sizeComputed:Boolean = false;
        
        
        /** The empty default constructor for the UStringHashMap object. */
        public function UStringHashMap() {
           //super();
           map = new Object();
        }
        
        /**
        * Delivers the internally managed map (Object instance).
        * 
        * @return the internally managed map.
        * */
        public function getMap():Object {
        	return map;
        } 
		
		/**
        * Sets the internally managed map (Object instance).
        * 
        * @param map the internally managed map to set.
        * */
		public function setMap(map:Object):void {
			this.map = map;
			this.size();
		}
		
        /** Removes all key/value pair mappings from this hashmap. */
        public function clear():void {
            for (var key:String in map)
                map[key] = null;
			this.length = 0;
        }
        
        /** 
		 * Delivers the value for the given key. 
		 * 
		 * @param key the key whose mapped value should be returned.
		 * @return the value object that is mapped to the given key or null.*/
        public function getValue(key:String):Object {
        	//if(map.hasOwnProperty(key))
        	if(map.hasOwnProperty(key))
        	   return map[key];
        	else 
        	   return null;
        }

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
        public function put(key:String, value:Object):Boolean {
            
			// key was already in the hashmap, so return true as indication.
			if(this.containsKey(key)) {
			   map[key] = value;
			   return true;
			}
			else {
			   map[key] = value;
			   this.length++;
			   return false;
			}
        }
        
        /**
         * Adds all key/value pairs of the given hashmap to this hashmap.
         * 
         * @param otherMap the specified hashmap whose key/value pairs should be added.
         * */
        public function putAll(otherMap:UStringHashMap):void {
            for(var key:String in otherMap.getMap()) 
               this.put(key,otherMap.getValue(key));
        }

		/**
		 * Removes the key and its mapped value from the hashmap.
		 * 
		 * @param key the key that should be removed from the hashmap.
		 * @return the mapped value, if the key could be removed and null otherwise.
		 * */
        public function remove(key:String):Object {
            if (this.containsKey(key)) {
				var value:Object = map[key];
				map[key] = null;
			    this.length--;
				return value;
			}
			else
			   return null;
        }
        
        /**
         * Removes all entries of the given hashmap from this hashmap.
         * 
         * @param otherMap the specified hashmap whose entires should be removed from this hashmap.
         * */
        public function removeAll(otherMap:UStringHashMap):void {
            for(var key:String in otherMap.getMap()) 
               this.remove(key); 
        }
        
        /** 
		 * Checks if the specified key is already in the hashmap (and mapped to a value). 
		 * 
		 * @param key the key that should be checked for existence in the hashmap.
		 * @return true, if the hashmap contains the given key and false otherwise.
		 * */
        public function containsKey(key:String):Boolean {
        	if(map[key] == null) 
        	    return false;
        	else
        	    return true;
        	//return map.hasOwnProperty(key); 
        }
        
        /** 
		 * Checks if this hashmap contains all keys of the specified hashmap. 
		 * 
		 * @param otherMap the other hashmap whose keys should be contained in this hashmap.
		 * @return true, if this hashmap contains all keys of the specified hashmap and false otherwise.
		 * */
        public function containsAllKeys(otherMap:UStringHashMap):Boolean {
        	for(var key:String in otherMap.getMap()) {
        	    if(!this.containsKey(key))
        	       return false;
        	}
        	return true;
        }
          
        /** 
		 * Checks if the specified value is already in the hashmap (and mapped to a key). 
		 * <p>
		 * This method iterates over all keys in the hashmap and determines if the specified 
		 * value is mapped to one of the found keys.
		 * 
		 * @param value the value that should be checked for existence in the hashmap.
		 * @return true, if the hashmap contains the given value and false otherwise.
		 * */
        public function containsValue(value:Object):Boolean {
        	for (var key:String in map) {
        		if(map[key] == value)
                    return true; 
        	}
        	return true;
        }
          
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
        public function size():int {
            /*var size:int = 0;
            for (var key:String in this)
                size++;*/
             
            if(!this.sizeComputed) {
               var number:int = 0;
               for (var key:String in map)
                  number++;
               this.length = number;
               this.sizeComputed = true;
            }    
            
            return this.length;
        }

		/**
		 * Delivers a string object consisting of the internally stored key/value pairs.
		 * 
		 * @return the string representation of the hashmap.
		 * */
        /*public function toString():String {
            var str:String;
            for (var key:String in map) {
                str += key + ": " + map[key].toString() + "\n";
            }
            return str;
        }*/

		/**
		 * Delivers an array representation of the internally stored key/value pairs.
		 * 
		 * @return the array representation.
		 * */
        public function values():ArrayCollection {
            var valueArray:ArrayCollection = new ArrayCollection();
            //for (var key:String in map)
              //  valueArray.addItem(map[key]);
            for each (var value:Object in map)
                valueArray.addItem(value);
            return valueArray;
        }
        
        /**
		 * Delivers an array representation of the internally stored key/value pairs.
		 * 
		 * @return the array representation in the native Array form.
		 * */
        public function valuesNative():Array {
            var valueArray:Array = new Array();
            //for (var key:String in map)
              //  valueArray.push(map[key]);
              for each (var value:Object in map)
                valueArray.push(value);
            return valueArray;
        }
        
		/**
		 * Checks whether this hashmap is empty or not.
		 * 
		 * @return true, if this hashmap is empty and false otherwise.
		 * */
        public function isEmpty():Boolean {
        	if(!this.sizeComputed) {
            	for(var key:String in map)
            	   return false; // we have at least on entry
            	return true; // no entry, because no iteration in the for-loop
            }
            else
                return (this.length == 0) ? true : false;
        }
        
		/** 
		 * Delivers an object (set) that stores all the keys of the hashmap. 
		 * 
		 * @return the keys of the hashmap stored in a set.
		 * */
        public function keySet():Object {
			
			var keys:Object = new Object ();
			
			for (var key:String in map) 
			   keys[key] = key;
			
			   return keys;
			   
		}
		
		/** 
		 * Delivers an array (ArrayCollection) that stores all the keys of the hashmap. 
		 * 
		 * @return the keys of the hashmap stored in an array.
		 * */
        public function keyArray():ArrayCollection {
			
			var keys:ArrayCollection = new ArrayCollection ();
			
			for (var key:String in map) 
			   keys.addItem(key);
			
			   return keys;
			   
		}
		
		 /**
         * Checks whether both hashmaps (this instance and the specified hashmap) contain the same 
         * entries or not. 
         * 
         * @param orherMap the specified hashmap which should be tested for equality.
         * @return true, if both maps are equal and false otherwise.
         * */
        public function equals(otherMap:UStringHashMap):Boolean {
        
            if(this.length != otherMap.size())
               return false;
        
            for (var key:String in otherMap.getMap()) {
               if(!this.containsKey(key))
                  return false;
               if(map[key] != otherMap.getValue(key))
                  return false;
            }
            
            return true;
        
        }
        
        /**
        * Delivers the string representation of the UStringHashMap object.
        * 
        * @return the string representation.
        * */
        public function toString():String {
        	
        	var printer:String = " StringHashMap is [";
        	
        	for (var key:String in map) {
        	   printer += " key:" + key + ",value:" + map[key] + " | ";
        	}
        	
        	printer += "]" + " and size is " + this.length;
        	
        	return printer;
        }
		

	}

}