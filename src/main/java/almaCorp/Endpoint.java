package almaCorp;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
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
	
	
	@ApiMethod(name = "followStatus", httpMethod = HttpMethod.GET, path ="followStatus")
    public Entity followStatus(@Named("follower") String follower, @Named("followed") String followed) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Verify if the user already follows this person
  		Query q =
  			    new Query("Follow")
  			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Follow", follower+"_"+followed)));
  		PreparedQuery pq = datastore.prepare(q);
  		int alreadyFollowed = pq.countEntities(FetchOptions.Builder.withLimit(1));

  		if (alreadyFollowed == 1) {
  			return new Entity("Reponse", "ok");
  		} else {
  			return new Entity("Reponse", "not ok");
  		}
		
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
			return new Entity("Reponse", "not ok");
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
		
		long newPostCount = (long) postingUser.getProperty("nbPosts") + 1;
		boolean receiversRemain = true;
		int offset = 0;
		Date date = new Date();
		String postId = DateUtils.createDate(date);
		
		//Update the user's number of posts
		postingUser.setProperty("nbPosts", newPostCount);
		datastore.put(postingUser);
				
		//Create the post
		Entity e = new Entity("Post", pseudo+"_"+postId);
		e.setProperty("pseudo", pseudo);
		e.setProperty("date", date);
		e.setProperty("image", body.getImage());
		e.setProperty("message", body.getMessage());
		e.setProperty("id", postId);
		datastore.put(e);
		
		//Create the likes counters
		for (int i=1; i<=10; i++) {
			Entity counter = new Entity("LikesCounter", pseudo+"_"+postId+"_"+i);
			counter.setProperty("likes", 0);
			datastore.put(counter);
		}
		
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
						Entity retrieveInfos = new Entity("RetrievePost", receiver+"_"+postId+"_"+pseudo+"_"+postId);
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
		
		//Fetch the post
		Query q =
		    new Query("Post")
		        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("Post", pseudo+"_"+postID)));
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		
		//Count likes
		q =
			    new Query("LikesCounter")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("LikesCounter", pseudo+"_"+postID+"_")));
		pq = datastore.prepare(q);
		List<Entity> counters = pq.asList(FetchOptions.Builder.withLimit(10));
		
		long totalLikes = 0;
		for (Entity entity : counters) {
			totalLikes += (long) entity.getProperty("likes");
		}
		
		result.setProperty("likes", totalLikes);
		
		
		return result;
	}
	
	@ApiMethod(name = "getProfile", httpMethod = HttpMethod.GET, path ="users/{pseudo}/posts")
    public List<Entity> getProfile(@Named("pseudo") String pseudo, @Named("offset") int offset) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
		//Get the 10 first posts destined to the user
		Query q =
                new Query("Post")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("Post", pseudo+"_")));
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> posts = pq.asList(FetchOptions.Builder.withLimit(10).offset(offset*10));
        List<Entity> result = new ArrayList<Entity>();
        
        for (Entity entity : posts) {
			String key = entity.getKey().getName();
			
			//Verify if the key matches the pseudo of the receiver
			if (key.startsWith(pseudo+"_")) {
				
				//Count likes
				q =
					    new Query("LikesCounter")
					        .setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("LikesCounter", pseudo+"_"+entity.getProperty("id")+"_")));
				pq = datastore.prepare(q);
				List<Entity> counters = pq.asList(FetchOptions.Builder.withLimit(10));
				
				long totalLikes = 0;
				for (Entity e : counters) {
					totalLikes += (long) e.getProperty("likes");
				}
				
				entity.setProperty("likes", totalLikes);
				//Add the post to the result list
		        result.add(entity);
				
			}
		}

        return result;
    }
	
	@ApiMethod(name = "refreshTimeline", httpMethod = HttpMethod.GET, path ="timeline")
    public List<Entity> refreshTimeline(@Named("pseudo") String pseudo, @Named("offset") int offset) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
		//Get the 10 first posts destined to the user
		Query q =
                new Query("RetrievePost")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("RetrievePost", pseudo+"_")));
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> posts = pq.asList(FetchOptions.Builder.withLimit(10).offset(offset*10));
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
		        Entity post = pq.asSingleEntity();
		        
		        //Count likes
				q =
					    new Query("LikesCounter")
					        .setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("LikesCounter", postId+"_")));
				pq = datastore.prepare(q);
				List<Entity> counters = pq.asList(FetchOptions.Builder.withLimit(10));
				
				long totalLikes = 0;
				for (Entity e : counters) {
					totalLikes += (long) e.getProperty("likes");
				}
		        post.setProperty("likes", totalLikes);
				
		        result.add(post);
				
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
  		
  		
		//Update one of the like counters
  		Random randomGenerator = new Random();
  		int randomInt = randomGenerator.nextInt(10)+1;
		q =
                new Query("LikesCounter")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("LikesCounter", poster+"_"+postID+"_"+randomInt)));        
		
        Transaction transaction = datastore.beginTransaction();
        try {
			pq = datastore.prepare(q);
			Entity counter = pq.asSingleEntity();
			long likes = (long) counter.getProperty("likes");
			counter.setProperty("likes", likes+1);
			datastore.put(counter);
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
		
		Random randomGenerator = new Random();
  		int randomInt = randomGenerator.nextInt(10)+1;
		Query q =
                new Query("LikesCounter")
                	.setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("LikesCounter", poster+"_"+postID+"_"+randomInt)));        
		
        Transaction transaction = datastore.beginTransaction();
        try {
			PreparedQuery pq = datastore.prepare(q);
			Entity counter = pq.asSingleEntity();
			long likes = (long) counter.getProperty("likes");
			counter.setProperty("likes", likes-1);
			datastore.put(counter);
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
	public Entity populateBenchmark(@Named("firstFollowerID") int firstFollowerID, @Named("lastFollowerID") int lastFollowerID) {
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
		for (int i=firstFollowerID; i<=lastFollowerID; i++) {
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
		
		//Fetch the serial poster
		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", "serialPoster"))); 
		PreparedQuery pq = datastore.prepare(q);
		Entity serialPoster = pq.asSingleEntity();
		
		Text imagePosted = new Text("data:image/png;base64,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"
				+ "U5ErkJggg==");
		
		
		//Generate posts
		for (int i = 1; i <= nbPosts; i++) {
			
			Date date = new Date();
			String postId = DateUtils.createDate(date);
					
			//Create the post
			Entity post = new Entity("Post", "serialPoster_"+postId);
			post.setProperty("pseudo", "serialPoster");
			post.setProperty("date", date);
			post.setProperty("image", imagePosted);
			post.setProperty("message", "test timeline");
			post.setProperty("id", postId);
			datastore.put(post);
			
			//Create the likes counters
			for (int j=1; j<=10; j++) {
				Entity counter = new Entity("LikesCounter", "serialPoster_"+postId+"_"+j);
				counter.setProperty("likes", 0);
				datastore.put(counter);
			}
			
			//Create retrieve infos
			Entity retrieveInfos = new Entity("RetrievePost", "poster_"+postId+"_serialPoster_"+postId);
			datastore.put(retrieveInfos);
				
		}
		
		//Update the post counter
		long previousNbPosts = (long) serialPoster.getProperty("nbPosts");
		serialPoster.setProperty("nbPosts", previousNbPosts+nbPosts);
		datastore.put(serialPoster);
		
		return new Entity("Reponse", "ok");
	}
	
	@ApiMethod(name = "refreshTimelineBenchmark", httpMethod = HttpMethod.GET, path ="timelineBenchmark")
    public List<Entity> refreshTimelineBenchmark(@Named("pseudo") String pseudo, @Named("nbPosts") int nbPosts) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
		//Get the posts destined to the user
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
		        Entity post = pq.asSingleEntity();
		        
		        //Count likes
				q =
					    new Query("LikesCounter")
					        .setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("LikesCounter", postId+"_")));
				pq = datastore.prepare(q);
				List<Entity> counters = pq.asList(FetchOptions.Builder.withLimit(10));
				
				long totalLikes = 0;
				for (Entity e : counters) {
					totalLikes += (long) e.getProperty("likes");
				}
		        post.setProperty("likes", totalLikes);
				
		        result.add(post);
			}
		}

        return result;
    }
	
	@ApiMethod(name = "likeBenchmark", httpMethod = HttpMethod.GET, path ="likeBenchmark/{poster}/{postID}")
    public Entity likeBenchmark(@Named("poster") String poster, @Named("postID") String postID) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Thread[] th=new Thread[2];
		Random random=new Random();		
		
		for (int i=0;i<th.length;i++) {			
			th[i]=ThreadManager.createThreadForCurrentRequest(new Runnable()  {
				public void run() {
					DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
					for (int j=0;j<50;j++) {
						Transaction txn=ds.beginTransaction();
						try {
							int randomInt=random.nextInt(10)+1;
							Query q =
					                new Query("LikesCounter")
					                	.setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("LikesCounter", poster+"_"+postID+"_"+randomInt)));
							PreparedQuery pq = datastore.prepare(q);
							Entity c = pq.asSingleEntity();
							Long likes = (Long)c.getProperty("likes");
							// UN SLEEP DE CONTENTION
							Thread.sleep(20);
							c.setProperty("likes", likes+1);
							datastore.put(c);
							txn.commit();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							if (txn.isActive()) {
							    txn.rollback();
							  }
						}
					}
				}
			});
			th[i].start();
		}

		for (Thread thread : th) {
			try {
				thread.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		Query q =
			    new Query("LikesCounter")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.GREATER_THAN, KeyFactory.createKey("LikesCounter", poster+"_"+postID+"_")));
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> counters = pq.asList(FetchOptions.Builder.withLimit(10));
		
		long totalLikes = 0;
		for (Entity entity : counters) {
			totalLikes += (long) entity.getProperty("likes");
		}
		
		return new Entity("Reponse", totalLikes);
	}
}


