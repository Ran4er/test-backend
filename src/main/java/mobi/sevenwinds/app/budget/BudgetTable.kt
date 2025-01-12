package mobi.sevenwinds.app.budget

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.CurrentDateTime

object BudgetTable : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val authorId = reference("author_id", AuthorTable).nullable()
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var type by BudgetTable.type
    var author by AuthorEntity.optionalReferencedOn(BudgetTable.authorId)

    fun toResponse(): BudgetRecord {
        return BudgetRecord(year, month, amount, type, author?.toResponse())
    }
}

object AuthorTable : IntIdTable("author") {
    val fullname = text("fullname")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime())
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullname by AuthorTable.fullname
    var createdAt by AuthorTable.createdAt

    fun toResponse(): AuthorResponse {
        return AuthorResponse(id.value, fullname, createdAt.toString())
    }
}

data class AuthorResponse(
    val id: Int,
    val fullname: String,
    val createdAt: String
)