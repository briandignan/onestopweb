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
        User(NotAssigned, "briandignan@gmail.com", "Brian Dignan", "password"),
        User(NotAssigned, "dosetareh@gmail.com", "Ariel Dosetareh", "password"),
        User(NotAssigned, "brianwei@terpmail.umd.edu", "Brian Weinblatt", "password"),
        User(NotAssigned, "ramaswamy.adithya@gmail.com", "Adithya Ramaswamy", "password"),
        User(NotAssigned, "eamare09@gmail.com", "Ebenezer Amare", "password"),
        User(NotAssigned, "jdephillipii@gmail.com", "Jim Dephillip", "password")
      ).foreach(User.create)
      
    }
    
  }
  
}