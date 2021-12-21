import org.jsoup._

import java.io.{BufferedWriter, File, FileWriter}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import java.io.{File, PrintWriter}
import Helpers._
import ch.qos.logback.classic.{Level, LoggerContext}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters.{and, equal, exists, gt, lt, lte, or, size}
import org.mongodb.scala.model.Indexes.descending
import org.mongodb.scala.model.Projections.{exclude, excludeId, fields}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala._
import org.slf4j.LoggerFactory

import scala.io.Source



object main extends App {
  val page = "http://www.vnukovo.ru/flights/online-timetable/#tab-sortie";

  //parser(page)
  writeToDB()
  mongoQueries()

  private def parser(_page: String) = {
    val doc = Jsoup.connect(_page).get()
    val table = doc.select("table[class=schedule-table-inner blue table-dflight-wrap]").select("tr")
    var i : Int = 0
    while (!table.eq(i).hasClass("visualyhidden")) {
      val columns = table.eq(i).select("td")
      val Array(time, date) = columns.eq(0).text().split(" ")
      val Array(day, mounth, year) = date.split('.')
      val Array(hours, minutes) = time.split(":")
      val flightNumber = columns.eq(1).text() // Flight number
      val airline = columns.eq(2).text() // Airline
      val destCity = columns.eq(3).text() // City
      val status = columns.eq(5).text() // Status

      val jsondoc =
        ("datetime" ->
          ("day" -> day.toInt) ~
          ("mounth" -> mounth.toInt) ~
          ("year" -> year.toInt) ~
          ("hours" -> hours.toInt) ~
          ("minutes" -> minutes.toInt)
        ) ~
        ("flightNumber" -> flightNumber) ~
        ("airline" -> airline) ~
        ("destCity" -> destCity) ~
        ("status" -> status)

      writeFile(compactRender(jsondoc), s"Row${(i+1).toString}")
      i += 1
    }
    println("Parsing completed!")
  }

  private def writeFile(json: String, name: String): Unit = {
    val dir = new File("E:\\Programming\\IdeaProjects\\flightsdb\\JSON\\").mkdir()
    val file = new File("E:\\Programming\\IdeaProjects\\flightsdb\\JSON\\" + s"${name}" + ".json")
    val buffer = new BufferedWriter(new FileWriter(file))
    buffer.write(json)
    buffer.close()
  }

  // MongoDB part
  private def writeToDB() = {
    // Connecting to Database
    val uri = "mongodb://localhost:27017"
    val mongoClient : MongoClient = MongoClient(uri)
    val mongoDB : MongoDatabase = mongoClient.getDatabase("laba4")
    val mongoCollection = mongoDB.getCollection("flights")

    // Logger Settings
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger1 = loggerContext.getLogger("org.mongodb.driver")
    val rootLogger2 = loggerContext.getLogger("reactor.util.Loggers")
    rootLogger1.setLevel(Level.OFF)
    rootLogger2.setLevel(Level.OFF)

    // Loading JSON files
    val files = getListOfFiles("E:\\Programming\\IdeaProjects\\flightsdb\\JSON")
    for (i <- files.indices) {
      val temp = Source.fromFile(files(i))
      mongoCollection.insertOne(BsonDocument(temp.mkString)).results()
      temp.close()
    }

    mongoClient.close()
    println("Writing to Database and Queries are completed!")
  }

  // MongoDB Queries
  private def mongoQueries() = {
    val uri = "mongodb://localhost:27017"
    val client: MongoClient = MongoClient(uri)
    val db: MongoDatabase = client.getDatabase("laba4")
    val collection: MongoCollection[Document] = db.getCollection("flights")

    println("Flights departing earlier than 9:00:")
    collection
      .find(lt("datetime.hours", 9))
      .sort(descending("datetime.hours"))
      .projection(excludeId())
      .printResults()
    println("")

    println("Flights to Saint Petersburg:")
    collection
      .find(equal("destCity", "Санкт-Петербург (Пулково)"))
      .projection(fields(excludeId()))
      .printResults()
    println("")

    println("Utair flights:")
    collection
      .find(equal("airline", "Utair"))
      .projection(fields(excludeId()))
      .printResults()
    println("")


    println("Rossiya flights to Saint Petersburg:")
    collection
      .find(
        and(equal("airline", "Россия"), equal("destCity", "Санкт-Петербург (Пулково)"))
      )
      .projection(fields(excludeId()))
      .printResults()
    println("")

    println("Flights to St. Petersburg from 12:00 to 6:00:")
    collection
      .find(
        and(
          and(equal("airline", "Россия"), lt("datetime.hours", 18)),
          gt("datetime.hours", 12)
        )
      )
      .projection(fields(excludeId()))
      .printResults()
    println("")
  }

  private def getListOfFiles(path: String): List[File] = {
    val directory = new File(path)
    if (directory.exists && directory.isDirectory) {
      directory.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

}



