package my.ktbot.utils.mihoyo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TaskList(
	@SerialName("already_received_points")
	val alreadyReceivedPoints: Int, // 110
	@SerialName("can_get_points")
	val canGetPoints: Int, // 0
	@SerialName("is_unclaimed")
	val isUnclaimed: Boolean, // false
	@SerialName("states")
	val states: List<State>,
	@SerialName("today_total_points")
	val todayTotalPoints: Int, // 110
	@SerialName("total_points")
	val totalPoints: Int, // 1997
) {
	@Serializable
	class State(
		@SerialName("happened_times")
		val happenedTimes: Int, // 1
		@SerialName("is_get_award")
		val isGetAward: Boolean, // true
		@SerialName("mission_id")
		val missionId: Int, // 58
		@SerialName("mission_key")
		val missionKey: String, // continuous_sign
		@SerialName("process")
		val process: Int, // 1
	)

}
