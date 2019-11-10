package almaCorp;

import java.util.Date;
import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@Api(name = "tinyInstaAPI",
version = "v1")

public class Endpoint {
	
	@ApiMethod(name = "addUser", httpMethod = HttpMethod.POST, path ="users")
	public Entity addUser(@Named("pseudo") String pseudo, @Named("nom") String nom, @Named("prenom") String prenom) {
		long nbPosts_init = 0;
		
		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo))); 
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		int alreadyTaken = pq.countEntities(FetchOptions.Builder.withLimit(1));
		
		Entity e = new Entity("User", pseudo);
		e.setProperty("pseudo", pseudo);
		e.setProperty("nom", nom);
		e.setProperty("prenom", prenom);
		e.setProperty("nbPosts", nbPosts_init);
		
		if (alreadyTaken==0) {
			datastore.put(e);
		} else {
			//throw IllegalArgumentException();
		}
		
		return e;
	}
	
	@ApiMethod(name = "removeUser", httpMethod = HttpMethod.DELETE, path ="users/{pseudo}")
	public Entity removeUser(@Named("pseudo") String pseudo) {
		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo)));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		
		datastore.delete(result.getKey());
		
		return result;
	}
	
	@ApiMethod(name = "follow", httpMethod = HttpMethod.PUT, path = "users/{follower}")
    public Entity follow(@Named("follower") String follower, @Named("followed") String followed) {
		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", follower)));
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		int followerExists = pq.countEntities(FetchOptions.Builder.withLimit(1));

		q =
		    new Query("User")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", follower)));
		pq = datastore.prepare(q);
		int followedExists = pq.countEntities(FetchOptions.Builder.withLimit(1));

		Entity e = new Entity("Follow", follower+"_"+followed);
		
		if (followerExists==1 && followedExists==1) {
	        datastore.put(e);
		} else {
			//throw ;
		}

        return e;
    }
	
	@ApiMethod(name = "listAllUsers", httpMethod = HttpMethod.GET, path = "users")
	public List<Entity> listAllUsers() {
		Query q =
		    new Query("User");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		
		return result;
	}
	
	@ApiMethod(name = "getUser", httpMethod = HttpMethod.GET, path = "users/{pseudo}")
	public Entity getUser(@Named("pseudo") String pseudo) {
		Query q =
		    new Query("User")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo)));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		
		
		return result;
	}
	
	@ApiMethod(name = "post", httpMethod = HttpMethod.POST, path ="users/{pseudo}/posts")
	public Entity post(@Named("pseudo") String pseudo, @Named("message") String message) {
		Query q =
		    new Query("User")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo)));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		long id = (long) result.getProperty("nbPosts") + 1;
		result.setProperty("nbPosts", id);
		
		Entity e = new Entity("Post", pseudo+"_"+id);
		e.setProperty("pseudo", pseudo);
		e.setProperty("message", message);
		e.setProperty("date", new Date());
		
		datastore.put(result);
		datastore.put(e);
		
		return e;
	}
	
	@ApiMethod(name = "refreshTimeline", httpMethod = HttpMethod.GET, path ="timeline")
    public Entity refreshTimeline(@Named("pseudo") String pseudo) {
        Query q =
                new Query("Post");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        Entity result = pq.asSingleEntity();

        return result;
    }
	
}
