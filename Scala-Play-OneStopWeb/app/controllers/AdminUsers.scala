package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import play.api.templates.HtmlFormat.Appendable
import anorm._

import models._
import views._

object AdminUsers extends Controller with Secured {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.AdminUsers.list())
  
  /**
   * Describe the computer form (used in both edit and create screens).
   */ 
  val userEditForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "email" -> email,
      "name" -> nonEmptyText,
      "password" -> default(text, "")
    )(User.apply)(User.unapply)
  )
  
  val userCreateForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "email" -> email,
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  )
	
  def listOld(page: Int, orderBy: Int, filter: String) = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user =>
      Ok(html.adminUserListOld(
      		user, 
      		User.list(page, 5, orderBy, "%"+filter+"%"),
      		orderBy, 
      		filter))
    }.getOrElse(Forbidden)
  }
  
  	
  def list() = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user =>
      Ok(html.adminUserList(
      		user, 
      		User.list(pageSize = 10000)
      		))
    }.getOrElse(Forbidden)
  }
  
  def create = IsAuthenticated {
  	username => implicit request =>
    User.findByEmail(username).map { user =>
      Ok(html.adminUserCreate(user, userCreateForm))
    }.getOrElse(Forbidden)
  }
  
  def addUser = IsAuthenticated {
  	username => implicit request =>
    User.findByEmail(username).map { user =>
	    userCreateForm.bindFromRequest.fold(
	      formWithErrors => BadRequest(html.adminUserCreate(user, formWithErrors)),
	      userToAdd => {
	        User.create(userToAdd)
	        Home.flashing("success" -> "User %s has been created".format(userToAdd.name))
	      }
	    )
    }.getOrElse(Forbidden)
  }
  
  def edit(id: Long) = IsAuthenticated {
  	username => implicit request =>
    User.findByEmail(username).map { user =>
    	User.findById(id) match {
    		case Some(userToEdit) => {
    			 Ok(html.adminUserEdit(
    				user,
    				id,
    				userEditForm.fill(userToEdit)))
    		}
    		case None => {
    			NotFound
    		}
    	}
    }.getOrElse(Forbidden)
  }
  
  /**
   * Handle user deletion.
   */
  def delete(id: Long) = IsAuthenticated {
  	username => implicit request =>
    User.findByEmail(username).map { user =>
    	User.delete(id)
    	Home.flashing("success" -> "User has been deleted")
    }.getOrElse(Forbidden)
  }
  
  
  /**
   * Handle the 'edit form' submission 
   *
   * @param id Id of the user to edit
   */
  def update(id: Long) = IsAuthenticated {
  	username => implicit request =>
    User.findByEmail(username).map { user =>
	    userEditForm.bindFromRequest.fold(
	      formWithErrors => BadRequest(html.adminUserEdit(user, id, formWithErrors)),
	      userToUpdate => {
	        User.update(id, userToUpdate)
	        Home.flashing("success" -> "User %s has been updated".format(userToUpdate.name))
	      }
	    )
    }.getOrElse(Forbidden)
  }
  
  
 
	
}