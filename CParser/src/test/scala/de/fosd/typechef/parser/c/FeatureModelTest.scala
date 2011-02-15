/*
 * Created by IntelliJ IDEA.
 * User: kaestner
 * Date: 14.02.11
 * Time: 14:57
 */
package de.fosd.typechef.parser.c

import de.fosd.typechef.featureexpr.FeatureModel
import de.fosd.typechef.featureexpr.FeatureExpr._

object FeatureModelTest extends Application {

    val featuremodel = FeatureModel.createFromDimacsFile_2Var("2.6.33.3-2var.dimacs")
    //    val featuremodel = FeatureModel.createFromCNFFile("linux_2.6.28.6.fm.cnf")

    def d(n: String) = createDefinedExternal(n)
    def nd(n: String) = createDefinedExternal(n).not

    val formulas = List(
        d("CONFIG_DISCONTIGMEM") and nd("CONFIG_NEED_MULTIPLE_NODES"),
        (d("CONFIG_DISCONTIGMEM") and d("CONFIG_NEED_MULTIPLE_NODES")),
        (d("CONFIG_DISCONTIGMEM") and d("CONFIG_NEED_MULTIPLE_NODES")).not,
        ((nd("CONFIG_DISCONTIGMEM") or nd("CONFIG_NEED_MULTIPLE_NODES")) and ((d("CONFIG_SPARSEMEM_EXTREME") and d("CONFIG_SPARSEMEM")) or (nd("CONFIG_SPARSEMEM_EXTREME") and d("CONFIG_SPARSEMEM")))),
        (((nd("CONFIG_GENERIC_LOCKBREAK") or d("CONFIG_DEBUG_LOCK_ALLOC")) and nd("CONFIG_LOCK_STAT") and d("CONFIG_LOCKDEP") and ((nd("CONFIG_DEBUG_SPINLOCK") and nd("CONFIG_SMP")) or (d("CONFIG_PROVE_LOCKING") and d("CONFIG_DEBUG_LOCK_ALLOC") and (d("CONFIG_PROVE_LOCKING") or nd("CONFIG_DEBUG_LOCK_ALLOC"))) or d("CONFIG_LOCKDEP") or d("CONFIG_PROVE_LOCKING") or (d("CONFIG_GENERIC_LOCKBREAK") and nd("CONFIG_DEBUG_LOCK_ALLOC")) or nd("CONFIG_DEBUG_LOCK_ALLOC") or ((nd("CONFIG_GENERIC_LOCKBREAK") or d("CONFIG_DEBUG_LOCK_ALLOC")) and d("CONFIG_DEBUG_LOCK_ALLOC") and d("CONFIG_LOCKDEP") and d("CONFIG_PROVE_LOCKING") and (d("CONFIG_DEBUG_SPINLOCK") or d("CONFIG_SMP")) and (d("CONFIG_PROVE_LOCKING") or nd("CONFIG_DEBUG_LOCK_ALLOC")))) and (d("CONFIG_DEBUG_SPINLOCK") or d("CONFIG_SMP")) and ((d("CONFIG_GENERIC_LOCKBREAK") and nd("CONFIG_DEBUG_LOCK_ALLOC")) or (nd("CONFIG_DEBUG_SPINLOCK") and nd("CONFIG_SMP")) or nd("CONFIG_LOCKDEP") or nd("CONFIG_LOCK_STAT")) and ((nd("CONFIG_DEBUG_SPINLOCK") and nd("CONFIG_SMP")) or nd("CONFIG_PROVE_LOCKING") or d("CONFIG_LOCKDEP") or (d("CONFIG_GENERIC_LOCKBREAK") and nd("CONFIG_DEBUG_LOCK_ALLOC")) or (nd("CONFIG_PROVE_LOCKING") and d("CONFIG_DEBUG_LOCK_ALLOC")) or nd("CONFIG_DEBUG_LOCK_ALLOC")))
                ).implies((d("CONFIG_DEBUG_SPINLOCK") or nd("CONFIG_SMP")) and (d("CONFIG_DEBUG_SPINLOCK") or d("CONFIG_SMP"))),
        (((nd("CONFIG_GENERIC_LOCKBREAK") or d("CONFIG_DEBUG_LOCK_ALLOC")) and d("CONFIG_LOCKDEP") and ((nd("CONFIG_DEBUG_SPINLOCK") and nd("CONFIG_SMP")) or (d("CONFIG_PROVE_LOCKING") and d("CONFIG_DEBUG_LOCK_ALLOC") and (d("CONFIG_PROVE_LOCKING") or nd("CONFIG_DEBUG_LOCK_ALLOC"))) or d("CONFIG_LOCKDEP") or d("CONFIG_PROVE_LOCKING") or (d("CONFIG_GENERIC_LOCKBREAK") and nd("CONFIG_DEBUG_LOCK_ALLOC")) or nd("CONFIG_DEBUG_LOCK_ALLOC") or ((nd("CONFIG_GENERIC_LOCKBREAK") or d("CONFIG_DEBUG_LOCK_ALLOC")) and d("CONFIG_DEBUG_LOCK_ALLOC") and d("CONFIG_LOCKDEP") and d("CONFIG_PROVE_LOCKING") and (d("CONFIG_DEBUG_SPINLOCK") or d("CONFIG_SMP")) and (d("CONFIG_PROVE_LOCKING") or nd("CONFIG_DEBUG_LOCK_ALLOC")))) and (d("CONFIG_DEBUG_SPINLOCK") or d("CONFIG_SMP")) and d("CONFIG_LOCK_STAT") and ((nd("CONFIG_DEBUG_SPINLOCK") and nd("CONFIG_SMP")) or nd("CONFIG_PROVE_LOCKING") or d("CONFIG_LOCKDEP") or (d("CONFIG_GENERIC_LOCKBREAK") and nd("CONFIG_DEBUG_LOCK_ALLOC")) or (nd("CONFIG_PROVE_LOCKING") and d("CONFIG_DEBUG_LOCK_ALLOC")) or nd("CONFIG_DEBUG_LOCK_ALLOC")))).implies((d("CONFIG_DEBUG_SPINLOCK") or nd("CONFIG_SMP")) and (d("CONFIG_DEBUG_SPINLOCK") or d("CONFIG_SMP")))

    )


    for (form <- formulas) {
        println(
            (if (form.isTautology(featuremodel)) "tautology"
            else
            if (form.isContradiction(featuremodel)) "contradiction" else "satisfiable")
                    + ": " +
                    form)
    }


}