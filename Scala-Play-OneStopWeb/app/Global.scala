import play.api._

import models._
import anorm._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }
	  
}

object InitialData {
  
  def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)
  
  def insert() = {
    
    if(User.findAll.isEmpty) {
      
      Seq(
        User("briandignan@gmail.com", "Brian Dignan", "password"),
        User("dosetareh@gmail.com", "Ariel Dosetareh", "password"),
        User("brianwei@terpmail.umd.edu", "Brian Weinblatt", "password"),
        User("ramaswamy.adithya@gmail.com", "Adithya Ramaswamy", "password"),
        User("eamare09@gmail.com", "Ebenezer Amare", "password")
      ).foreach(User.create)
      
    }
    
  }
  
}