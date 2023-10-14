package com.rocketseat.todolist.controller;

import com.rocketseat.todolist.task.Task;
import com.rocketseat.todolist.task.TaskRepository;
import com.rocketseat.todolist.utils.Utils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody Task task, HttpServletRequest request){
        System.out.println("Chegou no controller ");
        //recebe o Id do usuário que veio pelo request
        var idUser = request.getAttribute("idUser");
        task.setIdUser((UUID) idUser);

        //Validação da data da tarefa

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("A data de inicio e término da tarefa deve ser maior que a data atual");
        }

        if (task.getStartAt().isAfter(task.getEndAt())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("A data de inicio não pode ser maior que a data de término");
        }

        var newTask= this.taskRepository.save(task);
        return ResponseEntity.ok(task);

    }

    //listar tarefas

    @GetMapping("/")
    public List<Task> list(HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        var list = this.taskRepository.findByIdUser((UUID) idUser);
        return list;
    }

    //atualizar tarefa

    @PatchMapping("/{id}")
    public ResponseEntity update(@RequestBody Task task, @PathVariable UUID id, HttpServletRequest request){

        //validações: 1- se o idTask é valido 2- se o usuário é dono da tarefa a ser alterada

        //1- busca a task pelo ID no banco de dados e valida se existe
        var taskId = this.taskRepository.findById(id).orElse(null);

        if(taskId == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A tarefa não existe");
        }

        //recupera o ID do usuário autenticado que fez a requisição
        var idAuthenticatedUser = request.getAttribute("idUser");

        //valida se o Id user da task no banco é igual ao idAuthenticatedUser do usuário autenticado
        if(!taskId.getIdUser().equals(idAuthenticatedUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Você não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(task, taskId);

        var taskUpdated = taskRepository.save(taskId);

        return ResponseEntity.ok(taskUpdated);

    }

}
