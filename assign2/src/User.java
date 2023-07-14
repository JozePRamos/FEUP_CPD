public class User {
  int id;
  String name;
  String pass;
  int score;
  boolean isLogIn = false;
  
  public User(int id, String name, String pass, int score){
    this.id = id;
    this.name = name;
    this.pass = pass;
    this.score = score;
  }

  public int getId(){
    return id;
  }

  public String getName(){
    return name;
  }

  public String getPass(){
    return pass;
  }

  public int getScore(){
    return score;
  }
  
  public void setId(int id){
    this.id = id;
  }

  public void setName(String name){
    this.name = name;
  }

  public void setPass(String pass){
    this.pass = pass;
  }

  public void setScore(int score){
    this.score = score;
  }

  public void logIn(){
    this.isLogIn = true;
  }

  public void logOff(){
    this.isLogIn = false;
  }

  public boolean getLogIn(){
    return isLogIn;
  }
}
