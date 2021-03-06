/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.lang.core.psi.ext

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import org.rust.ide.icons.RsIcons
import org.rust.lang.core.psi.*
import org.rust.lang.core.psi.RsElementTypes.DEFAULT
import org.rust.lang.core.stubs.RsTypeAliasStub
import javax.swing.Icon

sealed class RsTypeAliasOwner {
    object Free: RsTypeAliasOwner()
    class Trait(val trait: RsTraitItem): RsTypeAliasOwner()
    class Impl(val impl: RsImplItem): RsTypeAliasOwner()
}

val RsTypeAlias.owner: RsTypeAliasOwner get() {
    val stub = stub
    val stubOnlyParent = if (stub != null) stub.parentStub.psi else parent
    return when (stubOnlyParent) {
        is RsMembers -> {
            val grandDad = parent.parent
            when (grandDad) {
                is RsTraitItem -> RsTypeAliasOwner.Trait(grandDad)
                is RsImplItem -> RsTypeAliasOwner.Impl(grandDad)
                else -> error("unreachable")
            }
        }
        else -> RsTypeAliasOwner.Free
    }
}


val RsTypeAlias.default: PsiElement?
    get() = node.findChildByType(DEFAULT)?.psi


abstract class RsTypeAliasImplMixin : RsStubbedNamedElementImpl<RsTypeAliasStub>, RsTypeAlias {

    constructor(node: ASTNode) : super(node)

    constructor(stub: RsTypeAliasStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getIcon(flags: Int): Icon? = iconWithVisibility(flags, RsIcons.TYPE)

    override val isPublic: Boolean get() = RustPsiImplUtil.isPublic(this, stub)

    override val isAbstract: Boolean get() = typeReference == null

    override val crateRelativePath: String? get() = RustPsiImplUtil.crateRelativePath(this)
}
