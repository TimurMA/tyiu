package com.tyiu.scrumservice.model

import com.tyiu.ideas.model.dto.UserDTO
import com.tyiu.ideas.model.entities.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface ProjectMemberRepository: CoroutineCrudRepository<ProjectMember, String>{

    @Query("SELECT * FROM project_member WHERE project_id = :projectId")
    fun findMemberByProjectId(projectId: String): Flow<ProjectMember>
}

@Table
data class ProjectMember (
    val projectId:String?=null,
    val userId:String?=null,
    val teamId:String?=null,
    val projectRole: ProjectRole?=ProjectRole.MEMBER,
    val startDate:LocalDate? = LocalDate.now(),
    val finishDate:LocalDate? = null
)

data class ProjectMemberDTO(
    val userId:String?=null,
    val teamId:String?=null,
    var email:String? = null,
    var firstName:String? = null,
    var lastName:String? = null,
    val projectRole:ProjectRole?,
    val startDate:LocalDate?,
    val finishDate:LocalDate?,
)

enum class ProjectRole{
    TEAM_LEADER, INITIATOR, MEMBER
}

fun ProjectMember.toDTO():ProjectMemberDTO = ProjectMemberDTO(
    userId = userId,
    teamId = teamId,
    projectRole = projectRole,
    startDate = startDate,
    finishDate = finishDate,
)
