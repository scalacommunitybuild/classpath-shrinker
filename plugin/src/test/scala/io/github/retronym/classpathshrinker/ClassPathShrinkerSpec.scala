package io.github.retronym.classpathshrinker

import coursier.{Dependency, Module, Organization, ModuleName}
import io.github.retronym.classpathshrinker.TestUtil._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class ClassPathShrinkerSpec {
  object Dependencies {
    val commons =
      Dependency(Module(Organization("org.apache.commons"), ModuleName("commons-lang3")), "3.5")
    val guava = Dependency(Module(Organization("com.google.guava"), ModuleName("guava")), "21.0")
  }

  import Dependencies._

  @Test
  def `All is reported when nothing is used`(): Unit = {
    val testCode =
      """class A {
        |  println("Hello")
        |  scala.io.Source.fromFile("")
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(commons, guava))
    val usedEntries = Seq()
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Nothing is reported when everything is used`(): Unit = {
    val testCode =
      """object Demo {
        |  org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |  com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |}
      """.stripMargin
    val unusedEntries = Seq()
    val usedEntries = Coursier.getArtifacts(Seq(guava, commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Commons is reported when guava is used`(): Unit = {
    val testCode =
      """object Demo1 {
        |  com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(commons))
    val usedEntries = Coursier.getArtifacts(Seq(guava))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Guava is reported when commons is used`(): Unit = {
    val testCode =
      """object Demo {
        |  org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(guava))
    val usedEntries = Coursier.getArtifacts(Seq(commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Nothing is reported when everything is used in a method`(): Unit = {
    val testCode =
      """object Demo {
        |  def foo(): Unit = {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Seq()
    val usedEntries = Coursier.getArtifacts(Seq(guava, commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Commons is reported when guava is used in a method`(): Unit = {
    val testCode =
      """object Demo1 {
        |  def foo(): Unit = {
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(commons))
    val usedEntries = Coursier.getArtifacts(Seq(guava))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Guava is reported when commons is used in a method`(): Unit = {
    val testCode =
      """object Demo {
        |  def foo(): Unit = {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(guava))
    val usedEntries = Coursier.getArtifacts(Seq(commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Nothing is reported when everything is used in nested decl`(): Unit = {
    val testCode =
      """object Foo {
        |  class Bar {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Seq()
    val usedEntries = Coursier.getArtifacts(Seq(guava, commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Commons is reported when guava is used in nested decl`(): Unit = {
    val testCode =
      """object Demo1 {
        |  class Bar {
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(commons))
    val usedEntries = Coursier.getArtifacts(Seq(guava))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Guava is reported when commons is used in nested decl`(): Unit = {
    val testCode =
      """object Demo {
        |  class Bar {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(guava))
    val usedEntries = Coursier.getArtifacts(Seq(commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Nothing is reported when everything is used in nested pkg`(): Unit = {
    val testCode =
      """package demo {
        |  class Bar {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Seq()
    val usedEntries = Coursier.getArtifacts(Seq(guava, commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Commons is reported when guava is used in nested pkg`(): Unit = {
    val testCode =
      """package demo {
        |  class Bar {
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(commons))
    val usedEntries = Coursier.getArtifacts(Seq(guava))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Guava is reported when commons is used in nested pkg`(): Unit = {
    val testCode =
      """package demo {
        |  class Bar {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(guava))
    val usedEntries = Coursier.getArtifacts(Seq(commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Nothing is reported when everything is used in nested pkg object`(): Unit = {
    val testCode =
      """package object demo {
        |  class Bar {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Seq()
    val usedEntries = Coursier.getArtifacts(Seq(guava, commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Commons is reported when guava is used in nested pkg object`(): Unit = {
    val testCode =
      """package object demo {
        |  class Bar {
        |    com.google.common.base.Strings.commonPrefix("abc", "abcd")
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(commons))
    val usedEntries = Coursier.getArtifacts(Seq(guava))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Guava is reported when commons is used in nested pkg object`(): Unit = {
    val testCode =
      """package object demo {
        |  class Bar {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |  }
        |}
      """.stripMargin
    val unusedEntries = Coursier.getArtifacts(Seq(guava))
    val usedEntries = Coursier.getArtifacts(Seq(commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }

  @Test
  def `Nothing is reported even if commons has relative path`(): Unit = {
    val testCode =
      """package object demo {
        |  class Bar {
        |    org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY.length
        |  }
        |}
      """.stripMargin
    val unusedEntries = Seq()
    val usedEntries = Coursier.getArtifactsRelative(Seq(commons))
    val expectedWarning = ClassPathFeedback.createWarningMsg(unusedEntries)
    val allEntries = usedEntries ++ unusedEntries
    expectWarning(expectedWarning, extraClasspath = allEntries)(testCode)
  }
}
