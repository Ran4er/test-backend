package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.author?.id?.let { AuthorEntity.findById(it) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .leftJoin(AuthorTable, { authorId }, { id })
                .select { BudgetTable.year eq param.year }
                .apply {
                    param.authorFullName?.let { authorName ->
                        andWhere { AuthorTable.fullname like "%${authorName}%" }
                    }
                }
                .orderBy(BudgetTable.month to true, BudgetTable.amount to false)
                .limit(param.limit, param.offset)

            val total = query.count()
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }

    suspend fun addAuthor(body: AuthorRequest): AuthorResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fullname = body.fullname
            }
            return@transaction entity.toResponse()
        }
    }
}