package de.fosd.typechef.parser.test

import de.fosd.typechef.parser._
import junit.framework._;
import junit.framework.Assert._
import de.fosd.typechef.featureexpr._
import org.junit.Test
import de.fosd.typechef.parser.test.parsers._

class DigitList2ParserRepTest extends TestCase with DigitListUtilities {
    val newParser =
        new DigitList2Parser() {
            override type OptResult[T] = T
            override def myRepOpt[T](p: => MultiParser[T],
                                     productionName: String): MultiParser[List[OptResult[T]]] =
                repPlain(p)
            def digits: MultiParser[AST] =
                myRepOpt(digitList | (digit.map(One(_))), "digitList") ^^ (
                        //List[this.OptResult[AST]] -> DigitList[List[Opt[AST]]]
                        ((x: List[this.OptResult[Conditional[AST]]]) =>
                            (DigitList2(x.map((y: Conditional[AST]) => Opt(FeatureExpr.base, y))))))
        }

    def testError1() {
        val input = List(t("("), t("3", f1), t(")", f1.not), t(")"))
        val actual = newParser.parse(input)
        System.out.println(actual)
        actual match {
            case newParser.Success(ast, unparsed) => {
                fail("should not parse " + input + " but result was " + actual)
            }
            case newParser.NoSuccess(msg, unparsed, inner) =>

        }
    }

    @Test
    def testParseSimpleList() {
        {
            val input = List(t("("), t(")"))
            val expected = DigitList2(List())
            assertParseResult(expected, newParser.parse(input))
        }
        {
            val input = List(t("("), t("1"), t(")"))
            val expected = DigitList2(List(o(l1)))
            assertParseResult(expected, newParser.parse(input))
        }
        {
            val input = List(t("("), t("1"), t("2"), t(")"))
            val expected = DigitList2(List(o(l1), o(l2)))
            assertParseResult(expected, newParser.parse(input))
        }
    }

    implicit def makeConditional(x: AST): Conditional[AST] = One(x)

    @Test
    def testParseOptSimpleList1() {
        val input = List(t("("), t("1", f1), t("2", f1.not), t(")"))
        val expected =
            Choice(f1, outer(l1), outer(l2))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListFirst() {
        val input = List(t("("), t("1", f1), t("1"), t("2"), t(")"))
        val trail = List(l1, l2)
        val expected = Choice(f1, wrapList(l1 :: trail), wrapList(trail))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListLast() {
        val input = List(t("("), t("1"), t("2"), t("3", f1), t(")"))
        val begin = List(l1, l2)
        val expected = Choice(f1, wrapList(begin ++ List(l3)), wrapList(begin: _*))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListMid() {
        val input = List(t("("), t("1"), t("2", f1), t("3"), t(")"))
        val expected = Choice(f1, wrapList(l1, l2, l3), wrapList(List(l1, l3)))
        //DigitList2(List(o(l1), Opt(f1, l2), o(l3)))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl1() {
        val input = List(t("("), t("1"), t("2", f1), t("3", f2), t(")"))
        val expected =
            Choice(f1,
                Choice(f2,
                    wrapList(l1, l2, l3),
                    wrapList(l1, l2)),
                Choice(f2,
                    wrapList(l1, l3),
                    wrapList(l1)))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl2() {
        val input = List(t("("), t("1", f2), t("2", f1), t("3", f2), t(")"))
        val expected =
            Choice(f2,
                Choice(f1,
                    wrapList(l1, l2, l3),
                    wrapList(l1, l3)),
                Choice(f1,
                    wrapList(l2),
                    wrapList()))

        //DigitList2(List(Opt(f2, l1), Opt(f1, l2), Opt(f2, l3)))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl3() {
        val input = List(t("("), t("1", f2), t("2", f1), t("3", f2.not), t(")"))
        val expected =
            Choice(f2,
                Choice(f1,
                    wrapList(l1, l2),
                    wrapList(l1)),
                Choice(f1,
                    wrapList(l2, l3),
                    wrapList(l3)))
        //DigitList2(List(Opt(f2, l1), Opt(f1, l2), Opt(f2.not, l3)))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseOptSimpleListCompl4() {
        val input = List(t("("), t("1", f2), t("2", f2.not), t("3", f2.not), t(")"))
        val expected =
            Choice(f2,
                wrapList(l1),
                wrapList(l2, l3))
        //DigitList2(List(Opt(f2, l1), Opt(f2.not, l2), Opt(f2.not, l3)))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseInterleaved1() {
        val input = List(t("("), t("("), t("1"), t("2"), t(")"), t("3"), t(")"))
        val expected = wrapList(wrapList(l1, l2), l3)
        //DigitList2(List(o(DigitList2(List(o(l1), o(l2)))), o(l3)))
        assertParseResult(expected, newParser.parse(input))
    }
    def testParseInterleaved2() {
        val input = List(t("("), t("(", f1), t("1"), t("2"), t(")", f1), t("3"), t(")"))
        val expected = Choice(f1,
            wrapList(
                wrapList(l1, l2),
                l3),
            wrapList(l1, l2, l3))
        //DigitList2(List(o(l1), o(l2), o(l3)))
        assertParseResult(expected, newParser.parse(input))
    }

    def testNoBacktrace {
        val input = List(t("1"), t("("))
        val expected = l1
        var actual = newParser.parse(input)
        println(actual)
        actual match {
            case newParser.Success(ast, unparsed) => fail("expected error, found " + ast + " - " + unparsed)
            case newParser.NoSuccess(msg, unparsed, inner) =>
        }
    }

    def assertParseResult(expected: AST, actual: newParser.ParseResult[Conditional[AST]]) {assertParseResult(One(expected), actual)}
    def assertParseResult(expected: Conditional[AST], actual: newParser.ParseResult[Conditional[AST]]) {
        System.out.println(actual)
        actual match {
            case newParser.Success(ast, unparsed) => {
                assertTrue("parser did not reach end of token stream: " + unparsed, unparsed.atEnd)
                assertEquals("incorrect parse result", One(outer(expected)), ast)
            }
            case newParser.NoSuccess(msg, unparsed, inner) =>
                fail(msg + " at " + unparsed + " " + inner)
        }
    }
}
