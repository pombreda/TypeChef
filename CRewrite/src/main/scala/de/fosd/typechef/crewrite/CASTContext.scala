package de.fosd.typechef.crewrite

import de.fosd.typechef.featureexpr.FeatureExpr
import de.fosd.typechef.parser.c.AST
import de.fosd.typechef.conditional.Opt
import java.util.IdentityHashMap

trait CASTEnv {

  type ASTContext = (List[FeatureExpr], Any, Any, Any, List[Any])

  object EmptyASTEnv extends ASTEnv (new IdentityHashMap[Any, ASTContext]())

  // store context of an AST entry
  // e: AST => (lfexp: List[FeatureExpr] parent: AST, prev: AST, next: AST, children: List[AST])
  class ASTEnv (val astc: IdentityHashMap[Any, ASTContext]) {
    def add(elem: Any, newelemc: ASTContext) = {
      var curelemc: ASTContext = null
      var curastc = new IdentityHashMap[Any, ASTContext](astc)
      if (curastc.containsKey(elem)) curelemc = curastc.get(elem)
      else curelemc = (null, null, null, null, null)

      // lfexp; parent; prev; next; children
      if (curelemc._1 != newelemc._1 && newelemc._1 != null) { curelemc = curelemc.copy(_1 = newelemc._1)}
      if (curelemc._2 != newelemc._2 && newelemc._2 != null) { curelemc = curelemc.copy(_2 = newelemc._2)}
      if (curelemc._3 != newelemc._3 && newelemc._3 != null) { curelemc = curelemc.copy(_3 = newelemc._3)}
      if (curelemc._4 != newelemc._4 && newelemc._4 != null) { curelemc = curelemc.copy(_4 = newelemc._4)}
      if (curelemc._5 != newelemc._5 && newelemc._5 != null) { curelemc = curelemc.copy(_5 = newelemc._5)}

      curastc.put(elem, curelemc)
      new ASTEnv(curastc)
    }

    override def toString() = {
      var res = ""
      for (k <- astc.keySet().toArray) {
        res = res + k + " (" + k.hashCode() + ")" +
          "\n\t" + astc.get(k)._1 +
          "\n\t" + astc.get(k)._2 +
          "\n\t" + astc.get(k)._3 +
          "\n\t" + astc.get(k)._4 +
          "\n\t" + astc.get(k)._5 +
          "\n ########################################### \n"
      }
      res
    }
  }

    // create ast-neighborhood context for a given translation-unit
  def createASTEnv(a: Product, lfexp: List[FeatureExpr] = List(FeatureExpr.base)): ASTEnv = {
    assert(a != null, "ast elem is null!")
    handleASTElems(a, null, lfexp, EmptyASTEnv)
  }

  // handle single ast elements
  // handling is generic because we can use the product-iterator interface of case classes, which makes
  // neighborhood settings is straight forward
  private def handleASTElems[T, U](e: T, parent: U, lfexp: List[FeatureExpr], env: ASTEnv): ASTEnv = {
    e match {
      case l:List[_] => handleOptLists(l, parent, lfexp, env)
      case x:Product => {
        var curenv = env.add(e, (lfexp, parent, null, null, x.productIterator.toList))
        for (elem <- x.productIterator.toList) {
          curenv = handleASTElems(elem, x, lfexp, curenv)
        }
        curenv
      }
      case _ => env
    }
  }

  // handle list of Opt nodes
  // sets prev-next connections for elements and recursively calls handleASTElems
  private def handleOptLists[T](l: List[_], parent: T, lfexp: List[FeatureExpr], env: ASTEnv): ASTEnv = {
    var curenv = env

    // set prev and next and children
    for (e <- createPrevElemNextTuples(l)) {
      e match {
        case (prev, Some(elem), next) => {
          curenv = curenv.add(elem, (lfexp, parent, prev.getOrElse(null), next.getOrElse(null), elem.asInstanceOf[Product].productIterator.toList))
        }
        case _ => ;
      }
    }

    // recursive call
    for (o@Opt(f, e) <- l) {
      curenv = handleASTElems(e, o, f::lfexp, curenv)
    }
    curenv
  }

  // since we do not have an neutral element that does not have any effect on ast
  // we use null and Any to represent values of no reference
  private def createPrevElemNextTuples[T](l: List[T]): List[(Option[T],Option[T],Option[T])] = {
    val nl = l.map(Some(_))
    val p = None :: None :: nl
    val e = (None :: Nil) ++ (nl ++ (None :: Nil))
    val n = nl ++ (None :: None :: Nil)

    (p,e,n).zipped.toList
  }
}