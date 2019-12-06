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
import com.google.appengine.api.datastore.Transaction;

@Api(name = "tinyInstaAPI",
version = "v1")

public class Endpoint {
	
	
	@ApiMethod(name = "addUser", httpMethod = HttpMethod.POST, path ="users")
	public Entity addUser(@Named("pseudo") String pseudo, @Named("nom") String nom, @Named("prenom") String prenom, @Named("password") String password) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Get the salt for password encryption
		Query q =
			    new Query("Salt")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Salt", "salt"))); 
		PreparedQuery pq = datastore.prepare(q);
		Entity saltEntity = pq.asSingleEntity();
						
		String salt = (String) saltEntity.getProperty("salt");
		long nbPosts_init = 0;

		String securedPassword = PasswordUtils.generateSecurePassword(password, salt);
		
		//Search the datastore for the pseudo to verify if it's available
		q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo))); 
		pq = datastore.prepare(q);
		int alreadyTaken = pq.countEntities(FetchOptions.Builder.withLimit(1));
		
		if (alreadyTaken!=0) {
			return new Entity("Reponse", "not ok");
		}
		
		//Create the User Entity
		Entity e = new Entity("User", pseudo);
		e.setProperty("pseudo", pseudo);
		e.setProperty("nom", nom);
		e.setProperty("prenom", prenom);
		e.setProperty("nbPosts", nbPosts_init);
		e.setProperty("password", securedPassword);
		datastore.put(e);
		
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
	
	
	
	@ApiMethod(name = "follow", httpMethod = HttpMethod.PUT, path = "follow")
    public Entity follow(@Named("follower") String follower, @Named("followed") String followed) {
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
		
		if (followerExists!=1 || followedExists!=1) {
			return new Entity("Reponse", "not ok");
		}
		
		datastore.put(f);
        datastore.put(fb);
        
		return new Entity("Reponse", "ok");
    }
	
	
	@ApiMethod(name = "unfollow", httpMethod = HttpMethod.DELETE, path = "follow")
    public Entity unfollow(@Named("follower") String follower, @Named("followed") String followed) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.delete(KeyFactory.createKey("Follow", follower+"_"+followed));
		datastore.delete(KeyFactory.createKey("FollowedBy", followed+"_"+follower));
        
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
		
		if (result == null) {
			return new Entity("Reponse", "");
		} else {
			return result;
		}
	}
	
	
	
	@ApiMethod(name = "verifyLogin", httpMethod = HttpMethod.GET, path = "verify")
	public Entity verifyLogin(@Named("pseudo") String pseudo, @Named("password") String password) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Get the salt for password encryption
		Query q =
			    new Query("Salt")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Salt", "salt"))); 
		PreparedQuery pq = datastore.prepare(q);
		Entity saltEntity = pq.asSingleEntity();
						
		String salt = (String) saltEntity.getProperty("salt");
		
		//Get the User Entity with corresponding pseudo
		q =
		    new Query("User")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo)));
		pq = datastore.prepare(q);
		
		//Verify if the user exists
		if (pq.countEntities(FetchOptions.Builder.withLimit(1)) == 0) {
			return new Entity("Reponse", "not ok");
		}
		
		Entity user = pq.asSingleEntity();
		
		//Verify the password
		String securedPassword = (String) user.getProperty("password");
		boolean correctPassword = PasswordUtils.verifyUserPassword(password, securedPassword, salt);
		
		if (correctPassword) {
			return new Entity("Reponse", "ok");
		} else {
			return new Entity("Reponse", "not ok");		
		}
	}
	
	
	
	@ApiMethod(name = "post", httpMethod = HttpMethod.POST, path ="users/{pseudo}/posts")
	public Entity post(@Named("pseudo") String pseudo, PostRequestBody body) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Retrieve the posting user
		Query q;
		PreparedQuery pq;
		Entity postingUser;
		try {
			q = new Query("User")
			    .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", pseudo)));
			pq = datastore.prepare(q);
			postingUser = pq.asSingleEntity();
		} catch (NullPointerException e1) {
			return new Entity("Reponse", "not ok");
		}
		
		long postId = (long) postingUser.getProperty("nbPosts") + 1;
		boolean receiversRemain = true;
		int offset = 0;
		Date date = new Date();
		
		//Update the user's number of posts
		postingUser.setProperty("nbPosts", postId);
		datastore.put(postingUser);
				
		//Create the post
		Entity e = new Entity("Post", pseudo+"_"+postId);
		e.setProperty("pseudo", pseudo);
		e.setProperty("date", date);
		e.setProperty("image", body.getImage());
		e.setProperty("message", body.getMessage());
		e.setProperty("likes", 0);
		e.setProperty("id", postId);
		datastore.put(e);
		
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
		
		
		
		return new Entity("Reponse", "ok");
	}
	
	
	@ApiMethod(name = "getPost", httpMethod = HttpMethod.GET, path ="users/{pseudo}/posts/{postID}")
	public Entity getPost(@Named("pseudo") String pseudo, @Named("postID") String postID) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q =
		    new Query("Post")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Post", pseudo+"_"+postID)));
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		
		return result;
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
	
	
	@ApiMethod(name = "likePost", httpMethod = HttpMethod.PUT, path ="users/{poster}/posts/{postID}")
    public Entity likePost(@Named("poster") String poster, @Named("postID") String postID, @Named("liker") String liker) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Verify if the poster exists
  		Query q =
  			    new Query("User")
  			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", poster)));
  		PreparedQuery pq = datastore.prepare(q);
  		int posterExists = pq.countEntities(FetchOptions.Builder.withLimit(1));

  		//Verify if the liker exists
  		q =
  		    new Query("User")
  		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", liker)));
  		pq = datastore.prepare(q);
  		int likerExists = pq.countEntities(FetchOptions.Builder.withLimit(1));
  		
  		//Verify if the liker already liked this post
  		q =
  	  		    new Query("Like")
  	  		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Like", liker+"_"+poster+"_"+postID)));
  	  		pq = datastore.prepare(q);
  	  		int postAlreadyLiked = pq.countEntities(FetchOptions.Builder.withLimit(1));
  		
  		//If one of them is missing, don't like the post
  		if (posterExists!=1 || likerExists!=1 || postAlreadyLiked!=0) {
	        return new Entity("Reponse", "not ok");
  		}
  		
  		
		//Update the like counter on the Post Entity
		q =
                new Query("Post")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Post", poster+"_"+postID)));        
		
        Transaction transaction = datastore.beginTransaction();
        try {
			pq = datastore.prepare(q);
			Entity post = pq.asSingleEntity();
			long counter = (long) post.getProperty("likes");
			post.setProperty("likes", counter+1);
			datastore.put(post);
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (transaction.isActive()) {
			    transaction.rollback();
			  }
		}
        
  		//Add entities to tables managing likes
        Entity l = new Entity("Like", liker+"_"+poster+"_"+postID);
		Entity lb = new Entity("LikedBy", poster+"_"+postID+"_"+liker);
		
		datastore.put(l);
		datastore.put(lb);
        		
		return new Entity("Reponse", "ok");
	}
	
	
	@ApiMethod(name = "hasLiked", httpMethod = HttpMethod.GET, path ="users/{liker}/likes/{poster}/{postID}")
    public Entity hasLiked(@Named("poster") String poster, @Named("postID") String postID, @Named("liker") String liker) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Verify if the liker already liked the post
  		Query q =
  			    new Query("Like")
  			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Like", liker+"_"+poster+"_"+postID)));
  		PreparedQuery pq = datastore.prepare(q);
  		int alreadyLiked = pq.countEntities(FetchOptions.Builder.withLimit(1));

  		if (alreadyLiked == 1) {
  			return new Entity("Reponse", "ok");
  		} else {
  			return new Entity("Reponse", "not ok");
  		}
		
    }
	
	
	@ApiMethod(name = "unlikePost", httpMethod = HttpMethod.DELETE, path ="users/{poster}/posts/{postID}")
    public Entity unlikePost(@Named("poster") String poster, @Named("postID") String postID, @Named("liker") String liker) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q =
                new Query("Post")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Post", poster+"_"+postID)));        
		
        Transaction transaction = datastore.beginTransaction();
        try {
			PreparedQuery pq = datastore.prepare(q);
			Entity post = pq.asSingleEntity();
			long counter = (long) post.getProperty("likes");
			post.setProperty("likes", counter-1);
			datastore.put(post);
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (transaction.isActive()) {
			    transaction.rollback();
			  }
		}
		
		datastore.delete(KeyFactory.createKey("Like", liker+"_"+poster+"_"+postID));
		datastore.delete(KeyFactory.createKey("LikedBy", poster+"_"+postID+"_"+liker));
		
		return new Entity("Reponse", "ok");
	}
	
	
	//------------------------Benchmark methods---------------------------

	@ApiMethod(name = "populateBenchmark", httpMethod = HttpMethod.POST, path ="populateBenchmark")
	public Entity populateBenchmark(@Named("nbFollowers") int nbFollowers) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//Get the salt for password encryption
		Query q =
			    new Query("Salt")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Salt", "salt"))); 
		PreparedQuery pq = datastore.prepare(q);
		Entity saltEntity = pq.asSingleEntity();
						
		String salt = (String) saltEntity.getProperty("salt");
		long nbPosts_init = 0;
		
		//Create the unique poster User Entity
		String securedPassword = PasswordUtils.generateSecurePassword("poster", salt);
		Entity e = new Entity("User", "poster");
		e.setProperty("pseudo", "poster");
		e.setProperty("nom", "poster");
		e.setProperty("prenom", "un");
		e.setProperty("nbPosts", nbPosts_init);
		e.setProperty("password", securedPassword);
		datastore.put(e);
		
		//Generate all of the followers and necessary infos
		Entity f;
		Entity fb;
		for (int i=1; i<=nbFollowers; i++) {
			securedPassword = PasswordUtils.generateSecurePassword("follower"+i, salt);
			e = new Entity("User", "follower"+i);
			e.setProperty("pseudo", "follower"+i);
			e.setProperty("nom", "follower"+i);
			e.setProperty("prenom", "ze");
			e.setProperty("nbPosts", nbPosts_init);
			e.setProperty("password", securedPassword);
			datastore.put(e);
			
			f = new Entity("Follow", "follower"+i+"_poster");
			fb =new Entity("FollowedBy", "poster_follower"+i);
			datastore.put(f);
			datastore.put(fb);
		}
		
		return new Entity("Reponse", "ok");
	}
	
	@ApiMethod(name = "postBenchmark", httpMethod = HttpMethod.POST, path ="postBenchmark")
	public Entity postBenchmark(@Named("nbPosts") int nbPosts) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Retrieve the posting user
		Query q;
		PreparedQuery pq;
		Entity postingUser;
		try {
			q = new Query("User")
			    .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", "poster")));
			pq = datastore.prepare(q);
			postingUser = pq.asSingleEntity();
		} catch (NullPointerException e1) {
			return new Entity("Reponse", "not ok");
		}
		
		for (int i = 1; i <= nbPosts; i++) {
			long postId = (long) postingUser.getProperty("nbPosts") + i;
			boolean receiversRemain = true;
			int offset = 0;
			Date date = new Date();
			
			//Create the posts
			Entity e = new Entity("Post", "poster_" + postId);
			e.setProperty("pseudo", "poster");
			e.setProperty("date", date);
			e.setProperty("image", "test image");
			e.setProperty("message", "hello");
			e.setProperty("likes", 0);
			datastore.put(e);
			
			//Search all the followers of the poster and add retrieve infos
			while (receiversRemain) {
				q = new Query("FollowedBy").setFilter(new FilterPredicate("__key__", FilterOperator.GREATER_THAN,
						KeyFactory.createKey("FollowedBy", "poster_")));
				pq = datastore.prepare(q);
				List<Entity> receivers = pq.asList(FetchOptions.Builder.withLimit(10).offset(offset * 10));

				if (receivers.size() != 0) {

					for (Entity entity : receivers) {
						String key = entity.getKey().getName();

						//Verify if the key matches the pseudo of the poster
						if (key.startsWith("poster_")) {
							String receiver = key.substring(key.indexOf("_") + 1);
							Entity retrieveInfos = new Entity("RetrievePost",
									receiver + "_" + DateUtils.createDate(date) + "_poster_" + postId);
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
			
		}
		
		//Update the user's number of posts
		postingUser.setProperty("nbPosts", (long)postingUser.getProperty("nbPosts")+nbPosts);
		datastore.put(postingUser);
		
		return new Entity("Reponse", "ok");
	}
	
	@ApiMethod(name = "refreshTimelineBenchmark", httpMethod = HttpMethod.GET, path ="timelineBenchmark")
    public List<Entity> refreshTimelineBenchmark(@Named("pseudo") String pseudo, @Named("nbPosts") int nbPosts) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
		//Get the 10 first posts destined to the user
		Query q =
                new Query("RetrievePost")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("RetrievePost", pseudo+"_")));
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> posts = pq.asList(FetchOptions.Builder.withLimit(nbPosts));
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
