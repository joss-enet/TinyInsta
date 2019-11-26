package almaCorp;


import java.util.ArrayList;
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
	
	//Password handling variables
	int saltLength = 30;
	String salt = PasswordUtils.getSalt(saltLength);
	
	@ApiMethod(name = "addUser", httpMethod = HttpMethod.POST, path ="users")
	public Entity addUser(@Named("pseudo") String pseudo, @Named("nom") String nom, @Named("prenom") String prenom, @Named("password") String password) throws Exception {
		long nbPosts_init = 0;
		String securedPassword = PasswordUtils.generateSecurePassword(password, salt);
		
		//Search the datastore for the pseudo to verify if it's available
		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo))); 
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		int alreadyTaken = pq.countEntities(FetchOptions.Builder.withLimit(1));
		
		//Create the User Entity
		Entity e = new Entity("User", pseudo);
		e.setProperty("pseudo", pseudo);
		e.setProperty("nom", nom);
		e.setProperty("prenom", prenom);
		e.setProperty("nbPosts", nbPosts_init);
		e.setProperty("password", securedPassword);
		
		if (alreadyTaken==0) {
			datastore.put(e);
		} else {
			throw new Exception("Pseudo already taken");
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
		
		return new Entity("Reponse", "ok");
	}
	
	
	
	@ApiMethod(name = "follow", httpMethod = HttpMethod.PUT, path = "users/{follower}")
    public Entity follow(@Named("follower") String follower, @Named("followed") String followed) throws Exception {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Verify if the follower exists
		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", follower)));
		PreparedQuery pq = datastore.prepare(q);
		int followerExists = pq.countEntities(FetchOptions.Builder.withLimit(1));

		//Verify if the followed exists
		q =
		    new Query("User")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", followed)));
		pq = datastore.prepare(q);
		int followedExists = pq.countEntities(FetchOptions.Builder.withLimit(1));

		//Add entries to the tables keeping track of following matters
		Entity f = new Entity("Follow", follower+"_"+followed);
		Entity fb = new Entity("FollowedBy", followed+"_"+follower);
		
		if (followerExists==1 && followedExists==1) {
	        datastore.put(f);
	        datastore.put(fb);
		} else {
			throw new Exception("One person doesn't exist");
		}

		return new Entity("Reponse", "ok");
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
	
	
	
	@ApiMethod(name = "verifyLogin", httpMethod = HttpMethod.GET, path = "verify")
	public Entity verifyLogin(@Named("pseudo") String pseudo, @Named("password") String password) throws Exception {
		
		//Get the User Entity with corresponding pseudo
		Query q =
		    new Query("User")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo)));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		Entity user = pq.asSingleEntity();
		
		//Verify the password
		String securedPassword = (String) user.getProperty("password");
		boolean correctPassword = PasswordUtils.verifyUserPassword(password, securedPassword, salt);
		
		if (correctPassword) {
			return new Entity("Reponse", "ok");
		} else {
			throw new Exception("Bad password");
		}
	}
	
	
	
	@ApiMethod(name = "post", httpMethod = HttpMethod.POST, path ="users/{pseudo}/posts")
	public Entity post(@Named("pseudo") String pseudo, PostRequestBody body) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Retrieve the posting user
		Query q =
		    new Query("User")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo)));
		PreparedQuery pq = datastore.prepare(q);
		Entity postingUser = pq.asSingleEntity();
		
		long postId = (long) postingUser.getProperty("nbPosts") + 1;
		boolean receiversRemain = true;
		int offset = 0;
		Date date = new Date();
		
		//Search all the followers of the poster and add retrieve infos
		while (receiversRemain) {
			q =
			    new Query("FollowedBy")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("FollowedBy", pseudo+"_")));
			pq = datastore.prepare(q);
			List<Entity> receivers = pq.asList(FetchOptions.Builder.withLimit(10).offset(offset*10));
		
			if (receivers.size() != 0) {
				
				for (Entity entity : receivers) {
					String key = entity.getKey().getName();
					
					//Verify if the key matches the pseudo of the poster
					if (key.startsWith(pseudo+"_")) {
						String receiver = key.substring(key.indexOf("_")+1);
						Entity retrieveInfos = new Entity("RetrievePost", receiver+"_"+DateUtils.createDate(date)+"_"+pseudo+"_"+postId);
						datastore.put(retrieveInfos);
						
					} else {
						receiversRemain = false;
					}
				}
				
			} else {
				receiversRemain = false;
			}
			offset++;
		}
		
		//Update the user's number of posts
		postingUser.setProperty("nbPosts", postId);
		datastore.put(postingUser);
				
		//Create the post
		Entity e = new Entity("Post", pseudo+"_"+postId);
		e.setProperty("pseudo", pseudo);
		e.setProperty("date", date);
		e.setProperty("image", body.getImage());
		e.setProperty("message", body.getMessage());
		datastore.put(e);
		
		return new Entity("Reponse", "ok");
	}
	
	
	
	@ApiMethod(name = "refreshTimeline", httpMethod = HttpMethod.GET, path ="timeline")
    public List<Entity> refreshTimeline(@Named("pseudo") String pseudo) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
		//Get the 10 first posts destined to the user
		Query q =
                new Query("RetrievePost")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("RetrievePost", pseudo+"_")));
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> posts = pq.asList(FetchOptions.Builder.withLimit(10));
        List<Entity> result = new ArrayList<Entity>();
        
        for (Entity entity : posts) {
			String key = entity.getKey().getName();
			
			//Verify if the key matches the pseudo of the receiver
			if (key.startsWith(pseudo+"_")) {
				String[] keySplit = key.split("_");
				String postId = keySplit[2]+"_"+keySplit[3];
				
				//Add the post to the result list
				q =
	                new Query("Post")
	                	.setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Post", postId)));
		        pq = datastore.prepare(q);
		        result.add(pq.asSingleEntity());
				
			}
		}

        return result;
    }
	
}
