import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { HomeComponent } from './home/home.component';
import { ProfileComponent } from './profile/profile.component';
import { BoardAdminComponent } from './board-admin/board-admin.component';
import { BoardConcepteurComponent } from './board-concepteur/board-concepteur.component';
import { BoardParticipantComponent } from './board-participant/board-participant.component';

import { authInterceptorProviders } from './_helpers/auth.interceptor';
import { CreerCompetenceComponent } from './creer-competence/creer-competence.component';
import { CreerQuestionComponent } from './creer-competence/creer-question/creer-question.component';
import { CreerQuizzComponent } from './creer-quizz/creer-quizz.component';

@NgModule({
declarations: [
AppComponent,
LoginComponent,
RegisterComponent,
HomeComponent,
ProfileComponent,
BoardAdminComponent,
BoardConcepteurComponent,
BoardParticipantComponent,
CreerCompetenceComponent,
CreerQuestionComponent,
CreerQuizzComponent
],
imports: [
BrowserModule,
AppRoutingModule,
FormsModule,
HttpClientModule
],
providers: [authInterceptorProviders],
bootstrap: [AppComponent]
})
export class AppModule { }
