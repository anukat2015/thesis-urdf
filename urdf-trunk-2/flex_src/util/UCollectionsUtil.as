package util
{
	
public class UCollectionsUtil
{
	public function UCollectionsUtil()
	{
		//TODO: implement function 
	}
    
    /**
     * Searches the specified element in the given array.
	 * <p> 
	 * The used search-method is the <i> binary-search </i>.
	 * <p>
	 * The element to search for is of type <i> String </i>.
	 * 
     * @return the index where the searched element was found or <i> -1 </i>, 
	 *         if the element could not be found.
     */
    public static function binaryStringSearch(collection:Array, elementToSearch:String):int {
        var high:int = collection.length;
        var low:int = -1;
        while (high - low > 1) {
            var position:int = (low + high) / 2;
            if (collection[position] > elementToSearch)
                high = position;
            else
                low = position;
        }
        if (low == -1 || collection[low] !== elementToSearch)
            return -1;
        else 
            return low;
    }
	
	/**
     * Searches the specified element in the given array.
	 * <p> 
	 * The used search-method is the <i> binary-search </i>.
	 * <p>
	 * The element to search for is of type <i> int </i>.
	 * 
     * @return the index where the searched element was found or <i> -1 </i>, 
	 *         if the element could not be found.
     */
    public static function binaryIntSearch(collection:Array, elementToSearch:int):int {
        var high:int = collection.length;
        var low:int = -1;
        while (high - low > 1) {
            var position:int = (low + high) / 2;
            if (collection[position] > elementToSearch)
                high = position;
            else
                low = position;
        }
        if (low == -1 || collection[low] !== elementToSearch)
            return -1;
        else
            return low;
    }


}

}