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

	
  def list(page: Int, orderBy: Int, filter: String) = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user =>
      Ok(html.adminUserList(
      		user, 
      		User.list(page, 5, orderBy, "%"+filter+"%"),
      		orderBy, 
      		filter))
    }.getOrElse(Forbidden)
  }
  
  def create = IsAuthenticated {
  	username => _ =>
    User.findByEmail(username).map { user =>
      Ok
    }.getOrElse(Forbidden)
  }
  
  def edit(id: Long) = IsAuthenticated {
  	username => _ =>
    User.findByEmail(username).map { user =>
      Ok
    }.getOrElse(Forbidden)
  }
	
}