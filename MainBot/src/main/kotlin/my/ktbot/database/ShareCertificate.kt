package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

/**
 *
 * @author bin
 * @since 2022/1/6
 */

@Deprecated("")
interface ShareCertificate : Entity<ShareCertificate> {
    val autoid: Long
    var qq: Long
    var code: String
    var price: Float
    var position: Int

    companion object : Entity.Factory<ShareCertificate>()
}

@Deprecated("")
object TShareCertificate : Table<ShareCertificate>(
    tableName = "ShareCertificate",
    alias = "sc", entityClass = ShareCertificate::class
) {
    val autoid: Column<Long> = long("autoid").bindTo { it.autoid }.primaryKey()

    val qq: Column<Long> = long("qq").bindTo { it.qq }

    val code: Column<String> = varchar("code").bindTo { it.code }

    val price: Column<Float> = float("price").bindTo { it.price }

    val position: Column<Int> = int("position").bindTo { it.position }
}
