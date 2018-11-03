package testing;

import org.junit.Test;

import app_kvServer.Cache;
import app_kvServer.impl.FIFOCache;
import app_kvServer.impl.LFUCache;
import app_kvServer.impl.LRUCache;
import junit.framework.TestCase;

public class CacheTest extends TestCase {
	@Test
    public void testFIFOCache() {
    	Cache myCahche = new FIFOCache(3);
    	myCahche.add("one", "asdqweqwe");
    	myCahche.add("two", "bdssaob");
    	myCahche.add("three", "iasnbdosiab");
    	assertEquals(myCahche.get("three"), "iasnbdosiab");
    	myCahche.add("four", "ubwouqb23");
    	assertEquals(myCahche.get("four"), "ubwouqb23");
    	assertNull(myCahche.get("one"));
    }
	
	@Test
    public void testLFUCache() {
    	Cache myCahche = new LFUCache(3);
    	myCahche.add("one", "asdqweqwe");
    	myCahche.add("two", "bdssaob");
    	myCahche.add("three", "iasnbdosiab");
    	assertEquals(myCahche.get("three"), "iasnbdosiab");
    	assertEquals(myCahche.get("one"), "asdqweqwe");
    	myCahche.add("four", "ubwouqb23");
    	assertEquals(myCahche.get("four"), "ubwouqb23");
    	assertNull(myCahche.get("two"));
    }

	@Test
    public void testLRUCache() {
    	Cache myCahche = new LRUCache(3);
    	myCahche.add("one", "asdqweqwe");
    	myCahche.add("two", "bdssaob");
    	myCahche.add("three", "iasnbdosiab");
    	assertEquals(myCahche.get("three"), "iasnbdosiab");
    	myCahche.add("four", "ubwouqb23");
    	assertEquals(myCahche.get("four"), "ubwouqb23");
    	assertNull(myCahche.get("three"));
    }
	
}
