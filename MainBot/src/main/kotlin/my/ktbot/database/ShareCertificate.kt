package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

/**
 *
 * @author bin
 * @since 2022/1/6
 */

@Table(tableName = "ShareCertificate", tableClassName = "TShareCertificate", alias = "sc")
interface ShareCertificate : Entity<ShareCertificate> {
	@PrimaryKey
	val autoid: Long
	var qq: Long
	var code: String
	var price: Float
	var position: Int

	companion object : Entity.Factory<ShareCertificate>()
}
