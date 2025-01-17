package com.tyiu.scrumservice.controller

import com.tyiu.scrumservice.model.*
import com.tyiu.scrumservice.service.TaskService
import kotlinx.coroutines.flow.Flow
import org.springframework.util.StopWatch.TaskInfo
import org.springframework.web.bind.annotation.*
import java.math.BigInteger

@RestController
@RequestMapping("/api/v1/scrum-service/task")
class TaskController (private val taskService: TaskService  ) {

    @GetMapping("/projects/{projectId}/all")
    fun getAllTaskByProject(@PathVariable projectId: String): Flow<TaskDTO> = taskService.getAllTasksByProject(projectId)

    @GetMapping("projects/{projectId}")
    fun getAllTasksInBackLog(@PathVariable projectId: String): Flow<TaskDTO> = taskService.getAllTasksInBacklog(projectId)

    @GetMapping("projects/{projectId}/sprint/{sprintId}")
    fun getAllTasksInSprint(@PathVariable projectId: String, @PathVariable sprintId: String): Flow<TaskDTO> = taskService.getAllTasksInSprint(projectId, sprintId)

    @GetMapping("{id}")
    fun getOneTaskById(@PathVariable id: BigInteger): Flow<TaskDTO> = taskService.getOneTaskById(id)

    @GetMapping("/all")  // ВЫВОД ВСЕХ СОЗДАННЫХ ТАСКОВ ВО ВСЕХ ПРОЕКТАХ. НУЖНО НА ВРЕМЯ РАЗРАБОТКИ
    fun getAllTasks(): Flow<Task> = taskService.getAllTasks()

    @PostMapping("/add")
    suspend fun postCreateTask(@RequestBody taskDTO: TaskDTO): TaskDTO = taskService.createTask(taskDTO)

    @PutMapping("/status/change")
    suspend fun putTaskStatus (@RequestBody taskStatusRequest: TaskStatusRequest) = taskService.putTaskStatus(taskStatusRequest)

    @PutMapping("/{taskId}/update")
    suspend fun putUpdateTask (@PathVariable taskId: BigInteger,@RequestBody taskInfoRequest: taskInfoRequest) = taskService.putUpdateTask(taskId, taskInfoRequest)

    @DeleteMapping("{id}/delete")
    suspend fun deleteTask(@PathVariable id: BigInteger) = taskService.deleteTask(id)
}