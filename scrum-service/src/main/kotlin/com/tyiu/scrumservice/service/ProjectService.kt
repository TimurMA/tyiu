package com.tyiu.scrumservice.service

import com.tyiu.ideas.model.dto.IdeaMarketDTO
import com.tyiu.ideas.model.toDTO
import com.tyiu.scrumservice.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.await
import org.springframework.stereotype.Service

@Service
class ProjectService(
    private val teamMemberRepository: TeamMemberRepository,
    private val projectRepository: ProjectRepository,
    private val ideaRepository: IdeaRepository,
    private val teamRepository: TeamRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val projectMarksRepository: ProjectMarksRepository,
    private val taskMovementLogRepository: TaskMovementLogRepository,
    val template: R2dbcEntityTemplate,
    private val userRepository: UserRepository
) {
    //suspend везде но не там где GET
    //не делать много запросов в бд, лучше получить один раз модель и вставить ее в дто
    //не делать много преобразований из дто и в дто
    //return map можно убрать
    //статус Paused  запрос чтобы его поменять
    //отдельная модель
    //по названию функции в репозитории формируется запрос автоматически
    suspend fun projectToDTO(project: Project): ProjectDTO {
        val projects = project.toDTO()
        val ideaToProject = project.ideaId?.let { ideaRepository.findById(it) }
        projects.name = ideaToProject?.name
        projects.description = ideaToProject?.description
        projects.customer = ideaToProject?.customer
        projects.initiator = ideaToProject?.initiatorId?.let { userRepository.findById(it)?.toDTO()}
        projects.team = project.teamId?.let { teamRepository.findById(it) }?.toDTO()
        projects.members = project.id?.let{ getProjectMembers(it) }?.toList()
        return projects
    }

    fun getAllProjects(): Flow<ProjectDTO> = projectRepository.findAll().map { projectToDTO(it) }

    fun getYourProjects(userId: String): Flow<ProjectDTO> = projectRepository.findProjectByUserId(userId).map { projectToDTO(it) }

    fun getYourActiveProjects(userId: String): Flow<ProjectDTO> = projectRepository.findByStatus(userId).map { projectToDTO(it) }

    suspend fun getOneProject(projectId: String): ProjectDTO? = projectRepository.findById(projectId)?.let { projectToDTO(it) }

    fun getProjectMembers(projectId: String): Flow<ProjectMemberDTO>? =
        projectMemberRepository.findMemberByProjectId(projectId).map { p ->
            val projectMember = p.toDTO()
            val userToProject = p.userId?.let { userRepository.findById(it) }?.toDTO()
            projectMember.email = userToProject?.email
            projectMember.firstName = userToProject?.firstName
            projectMember.lastName = userToProject?.lastName
            return@map projectMember
        }

    fun getProjectMarks(projectId: String): Flow<ProjectMarks> = projectMarksRepository.findMarksByProjectId(projectId)

    fun getProjectLogs(projectId: String): Flow<TaskMovementLog> = taskMovementLogRepository.findLogByProjectId(projectId)

    suspend fun createProject(ideaMarketDTO: IdeaMarketDTO): ProjectDTO {
        val project = Project(
            ideaId = ideaMarketDTO.ideaId,
            teamId = ideaMarketDTO.team.id
        )
        val prjSave = projectRepository.save(project)
        prjSave.teamId?.let {
            teamMemberRepository.findMemberByTeamId(it).map {m->
                val projectMember = ProjectMember(
                projectId = prjSave.id,
                userId = m.userId,
                teamId = m.teamId
                )
                projectMemberRepository.save(projectMember)
            }
        }
        return projectToDTO(prjSave)
    }

    suspend fun addMembersInProject(projectId: String, teamMemberRequest: TeamMemberRequest): ProjectMemberDTO {
        val projectMember = ProjectMember(
            projectId = projectId,
            userId = teamMemberRequest.userId,
            teamId = teamMemberRequest.teamId
        )
        return projectMemberRepository.save(projectMember).toDTO()
    }

    suspend fun putProjectMarks(projectMarks: ProjectMarks) {
        val query = "UPDATE project_marks SET mark = :mark WHERE user_id = :userId"
        return template.databaseClient.sql(query)
            .bind("mark", projectMarks.mark!!)
            .bind("userId", projectMarks.userId!!).await()
    }

    suspend fun pauseProject(projectId: String) {
        val query = "UPDATE project SET status = 'PAUSED' WHERE id = :projectId"
        return template.databaseClient.sql(query).bind("projectId", projectId).await()
    }

    suspend fun putFinishProject(projectId: String,projectFinishRequest:ProjectFinishRequest) {
        val query = "UPDATE project SET finish_date = :finishDate, report = :projectReport, status = 'DONE' WHERE id = :projectId"
        return template.databaseClient.sql(query)
            .bind("finishDate", projectFinishRequest.finishDate!!)
            .bind("projectReport", projectFinishRequest.projectReport!!)
            .bind("projectId", projectId).await()
    }
}



