package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import anorm._

import models._
import views._

object OneStop extends Controller with Secured {

  /**
   * Display the home page.
   */
  def index = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.index(user))
    }.getOrElse(Forbidden("blah"))
  }
  
  /**
   * Display the home page.
   */
  def inventory = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.inventory(user))
    }.getOrElse(Forbidden("blah"))
  }
  
  /**
   * Display the home page.
   */
  def customers = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.customers(user))
    }.getOrElse(Forbidden("blah"))
  }
  
  /**
   * Display the home page.
   */
  def vendors = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.vendors(user))
    }.getOrElse(Forbidden("blah"))
  }
  
  /**
   * Display the home page.
   */
  def reports = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.reports(user))
    }.getOrElse(Forbidden("blah"))
  }
  
  /**
   * Display the home page.
   */
  def settings = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.settings(user))
    }.getOrElse(Forbidden("blah"))
  }

}