package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import play.api.templates.HtmlFormat.Appendable
import anorm._

import models._
import views._

object OneStop extends Controller with Secured {

	
  def index = authenticatedPage( html.index(_) )
  def inventory = authenticatedPage( html.inventoryList(_) )
  def customers = authenticatedPage( html.customerList(_) )
  def vendors = authenticatedPage( html.vendorList(_) )
  def reports = authenticatedPage( html.reports(_) )
  def settingsUsers = authenticatedPage( html.settingsUsers(_) )
  def settingsOther = authenticatedPage( html.settingsOther(_) )

  
  private def authenticatedPage( f: User => Appendable): EssentialAction = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(f(user))
    }.getOrElse(Forbidden)
  }
  
}