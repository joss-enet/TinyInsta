# TinyInsta

Projet réalisé par Josselin Enet, Kévin Roy et Tristan Lenormand
M1 ALMA 2019-2020

URL de notre application : https://tinyinsta-257216.appspot.com  
URL de notre API Explorer : http://apis-explorer.appspot.com/apis-explorer/?base=https://tinyinsta-257216.appspot.com/_ah/api&root=https://tinyinsta-257216.appspot.com/_ah/api  
  
Pour voir comment se comporte l'application lorsqu'il y a beaucoup de posts à afficher, rendez-vous sur le profil de "serialPoster" grâce à la barre de recherche située en haut de la Timeline.  
  
## Mesures

Afin d'obtenir des temps raisonnables lors de nos mesures, nous avons choisi d'utiliser une image très peu volumineuse (19kb). Les temps sont donc plus importants avec des images plus grosses, mais cela ne change rien à la scalabilité de l'application.  
  
**Temps nécessaire pour poster :**  

![Post time mesures](/images/post_mesures.png)  

![Post time per number of followers](/images/post_chart.png)  

**Temps nécessaire pour récupérer des posts :**  

![Fetch time mesures](/images/timeline_mesures.png)  

![Fetch time per number of posts](/images/timeline_chart.png)  

On obtient bien ce qui ressemble fortement à une droite, l'application semble donc pouvoir scaler.  

**Nombre de likes possibles par seconde :**  

![Likes per second](/images/likes_mesures.png)  

## Entités

Voici la liste des différentes entités utilisées :  
* Salt : garde en mémoire la chaîne permettant de crypter les mots de passe.  

![Salt entity screenshot](/images/salt.png)  

* User : contient les informations relatives à chaque compte d'utilisateur.  

![User entity screenshot](/images/user.png)  

* Follow : contient des clés formées comme tel : \[pseudo du follower\]\_\[pseudo de la personne suivie\]. Elle n'est pas utilisée dans l'application mais permettrait de facilement retrouver tous les comptes suivis par un utilisateur.  

![Follow entity screenshot](/images/follow.png)  

* FollowedBy : contient des clés identiques à celles présentes dans Follow, mais les deux propriétés ont été inversées. Elle est utilisée pour créer des entités RetrievePost (présentées plus loin) quand une personne poste quelque chose.  

![FollowedBy entity screenshot](/images/followedBy.png)  

* Post : contient des informations relative à un post, mais pas son nombre de likes. L'identifiant d'un post est construit de sorte que celui des posts les plus récents soit plus petit (selon l'ordre lexicographique) que celui des posts plus anciens. La clé du post est construite à partir du pseudo du poster et de cet identifiant.  

![Post entity screenshot](/images/post.png)  

* RetrievePost : contient des clés formées comme tel : \[pseudo du follower\]\_\[identifiant du post\]\_\[pseudo du poster\]\_\[identifiant du post\]. A chaque fois qu'une personne poste un message, une entité de ce type est créée pour chacun de ses followers. Grâce à l'identifiant du post, pour une même personne, les nouvelles entités sont placées avant les entités plus anciennes, ce qui permet de les trouver directement lorsqu'on souhaite charger les nouveaux posts.  

![RetrievePost entity screenshot](/images/retrievePost.png)  

* Like : contient des clés formées comme tel : \[pseudo du liker\]\_\[clé du post\]. Cela permet de savoir rapidement si un utilisateur a déjà liké un post ou non.  

![Like entity screenshot](/images/like.png)  

* LikedBy : contient des clés identiques à celles présentes dans Like, mais les deux propriétés ont été inversées. Elle n'est pas exploitée dans notre application mais permettrait de retrouver les utilisateurs ayant liké un post en particulier.  

![LikedBy entity screenshot](/images/likedBy.png)  

* LikesCounter : contient des clés formées comme tel : \[clé du post\]\_\[numéro du compteur\]. A la création d'un post, on créé 10 compteurs qui lui sont associés et sont initialisés à 0. Lorsqu'un utilisateur like ce post, on incrémente au hasard un de ses compteurs, ce qui réduit la contention. Pour obtenir le total de likes d'un post, il suffit d'additionner la valeur de chacun de ses 10 compteurs.  

![LikesCounter entity screenshot](/images/likesCounter.png)