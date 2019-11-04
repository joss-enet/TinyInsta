package almaCorp;

public class User {
	private String pseudo;
	private String nom; 
	private String prenom;
	
	public User() {};
	
	public User(String pseudo, String nom, String prenom) {
		super();
		this.pseudo = pseudo;
		this.nom = nom;
		this.prenom = prenom;
	}
	
	public String getPseudo() {
		return pseudo;
	}
	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	
	
}
