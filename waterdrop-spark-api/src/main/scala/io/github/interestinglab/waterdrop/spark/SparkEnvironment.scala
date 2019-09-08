package io.github.interestinglab.waterdrop.spark

import com.typesafe.config.{Config, ConfigFactory}
import io.github.interestinglab.waterdrop.env.RuntimeEnv
import io.github.interestinglab.waterdrop.plugin.CheckResult
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConversions._

class SparkEnvironment extends RuntimeEnv {

  private var sparkSession: SparkSession = _

  private var streamingContext: StreamingContext = _

  var config: Config = ConfigFactory.empty()

  override def setConfig(config: Config): Unit = this.config = config

  override def getConfig: Config = config

  override def checkConfig(): CheckResult = new CheckResult(true, "")

  override def prepare(): Unit = {
    val sparkConf = createSparkConf()
    sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
    createStreamingContext
  }

  private def createSparkConf(): SparkConf = {
    val sparkConf = new SparkConf()
    config.entrySet()
      .foreach(entry => {
        sparkConf.set(entry.getKey, String.valueOf(entry.getValue.unwrapped()))
      })
    sparkConf
  }

  private def createStreamingContext: StreamingContext = {
    val conf = sparkSession.sparkContext.getConf
    val duration = conf.getLong("spark.stream.batchDuration", 5)
    if (streamingContext == null) {
      streamingContext = new StreamingContext(sparkSession.sparkContext, Seconds(duration))
    }
    streamingContext
  }

  def getStreamingContext: StreamingContext = {
    streamingContext
  }

  def getSparkSession: SparkSession = {
    sparkSession
  }

}