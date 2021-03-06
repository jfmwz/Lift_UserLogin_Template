package code.model

import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.common.Loggable
import net.liftweb.common.Loggable
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.util.Helpers._
import org.bson.types.ObjectId
import java.text.ParsePosition

class Reminder extends MongoRecord[Reminder] with ObjectIdPk[Reminder] with Loggable {

  def meta = Reminder

  object owner extends ObjectIdRefField(this, User)
  object description extends StringField(this, 1200)
  object created extends DateField(this)
  object due extends DateField(this)

}

object Reminder extends Reminder with MongoMetaRecord[Reminder] {

  /**
   * For creating things to do of a user.
   */
  def createThingsToDo(id: String, description: String, due_date: String): Either[String, Boolean] = {
    if (description.equals(""))
      Left("Enter text")
    else if (due_date.equals(""))
      Left("Select date")
    else {
      try {
        val df = new SimpleDateFormat("MM/dd/yyyy");
        df.setLenient(false);
        val position = new ParsePosition(0);
        val stringToDate = df.parse(due_date, position);
        if (position.getIndex() == due_date.length()) {
          val thingsToDo = Reminder.createRecord
          thingsToDo.created(new Date())
          thingsToDo.due(stringToDate)
          thingsToDo.owner(new ObjectId(id))
          thingsToDo.description(description)
          thingsToDo.save
          Right(true)
        } else
          Left("Enter date in MM/dd/yyyy format")
      } catch {
        case ex => {
          Left("Enter date in MM/dd/yyyy format")
        }
      }
    }
  }

  def findAllNotes(userId: String) = Reminder.findAll((("owner" -> userId)))

  def findAllNotes(userId: String, sortOrder: Int, limit: Int, skip: Int) =
    {
      sortOrder match {
        case 0 => Reminder.findAll((("owner" -> userId)), ("created" -> -1), Skip(skip), Limit(limit))
        case _ => Reminder.findAll((("owner" -> userId)), ("due" -> sortOrder), Skip(skip), Limit(limit))
      }
    }

  /**
   * For deleting things to do of a user.
   */
  def deleteThingsToDo(thingsToDo: Reminder) = {
    Reminder.getThingsToDo(thingsToDo).delete_!
  }

  /**
   * For getting Things To Do reference of a user.
   */
  def getThingsToDo(thingsToDo: Reminder) = {
    Reminder.find(thingsToDo.id.toString).get
  }

  /**
   * For updating status of particular things to do of a user.
   */
  def updateThingsToDo(thingsToDo: Reminder, desc: String, date: String): Either[String, Boolean] = {
    if (desc.equals(""))
      Left("Can not updated. Enter Text")
    else if (date.equals(""))
      Left("Can not updated. Select date")
    else {
      try {
        val df = new SimpleDateFormat("MM/dd/yyyy");
        df.setLenient(false);
        val position = new ParsePosition(0);
        val stringToDate = df.parse(date, position);
        if (position.getIndex() == date.length()) {
          thingsToDo.description(desc).due(stringToDate).update
          Right(true)
        } else
          Left("Enter date in MM/dd/yyyy format")
      } catch {
        case ex => {
          Left("Enter date in MM/dd/yyyy format")
        }
      }
    }
  }
}

