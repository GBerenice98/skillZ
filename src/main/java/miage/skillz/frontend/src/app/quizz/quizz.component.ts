import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService } from '../services/quiz.service';
import { HttpClient } from '@angular/common/http';
import { Quiz } from '../models/quiz';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { Competence } from '../models/competence';
import { TokenStorageService } from '../_services/auth/service/token-storage.service';

@Component({
  selector: 'app-quizz',
  templateUrl: './quizz.component.html',
  styleUrls: ['./quizz.component.scss']
})
export class QuizzComponent implements OnInit,AfterViewInit  {

  public listQuiz : Array<Quiz> =[];
  private roles: string[] = [];
  isLoggedIn = false;
  showModeratorBoard = false;

  displayedColumns: string[] = ['num','theme','nom', 'niveau', 'competence','duree'];

  dataSource!: MatTableDataSource<Quiz>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private quizService : QuizService,private tokenStorageService: TokenStorageService) {
    this.dataSource = new MatTableDataSource(this.listQuiz);
  }

  ngOnInit(): void {

    this.isLoggedIn = !!this.tokenStorageService.getToken();

    if (this.isLoggedIn) {
      const user = this.tokenStorageService.getUser();
      this.roles = user.roles;
      this.showModeratorBoard = this.roles.includes('ROLE_CONCEPTEUR');
    }

    let listQuiz : Array<Quiz> =[];
    this.quizService.getAllQuiz().subscribe(data => {
      data.forEach(q => {  
        q.stringCompetence=this.quizCompetenceToString(q.quizCompetences);
        console.log(" stringCompetence :", q.stringCompetence);    
        listQuiz.push(q);
      })
      this.dataSource = new MatTableDataSource(this.listQuiz);
    });

    this.listQuiz=listQuiz;
    console.log("quiz : ",this.listQuiz);
    console.log("datasource : ", this.dataSource.data);
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  quizCompetenceToString(competences : Array<Competence>) : string
  {
    let stringArray='';
    competences.forEach(c => {
      stringArray+=c.nom_competence+",";
    })
    return stringArray;
  }

}
