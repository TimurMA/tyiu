package com.tyiu.scrumservice.model


import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.math.BigInteger
import java.time.LocalDate

interface SprintRepository: CoroutineCrudRepository<Sprint, String>{
    @Query("SELECT * FROM sprint WHERE project_id =:projectId")
    fun findAllSprintsByProject(projectId: String): Flow<Sprint>

    @Query("SELECT * FROM sprint WHERE id =:id ")
    fun findSprintById(id: BigInteger): Flow<Sprint>

    @Query("SELECT * FROM sprint WHERE project_id =:projectId and status = 'ACTIVE'")
    fun findActiveSprint(projectId: String): Flow<Sprint>


}
@Table

data class Sprint(
        @Id
        val id:String? = null,
        val projectId:String? = null,
        val name:String? = null,
        val goal:String? = null,
        val report:String? = null,
        val startDate:LocalDate? = LocalDate.now(),
        val finishDate:LocalDate? = LocalDate.now(),
        val workingHours:Long? = null,
        var status:SprintStatus? = SprintStatus.ACTIVE,
)

enum class SprintStatus{
    ACTIVE, DONE
}

data class SprintDTO(
        val id:String? = null,
        val projectId:String? = null,
        val name:String? = null,
        val report:String? = null,
        val startDate:LocalDate? = LocalDate.now(),
        val finishDate:LocalDate? = LocalDate.now(),
        val workingHours:Long? = null,
        val tasks:HashMap<TaskStatus, List<TaskDTO>>? = null,

)

fun Sprint.toDTO(): SprintDTO = SprintDTO (
        id = id,
        projectId = projectId,
        name = name,
        report = report,
        startDate = startDate,
        finishDate = finishDate,
        workingHours = workingHours,
)