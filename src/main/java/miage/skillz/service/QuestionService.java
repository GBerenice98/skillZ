package miage.skillz.service;

import lombok.extern.slf4j.Slf4j;
import miage.skillz.entity.*;
import miage.skillz.models.QuestionImpl;
import miage.skillz.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ReponseQuestionRepository repQuestionRepository;
    @Autowired
    private CompetenceRepository competenceRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private NiveauRepository niveauRepository;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<Question> createQuestion(QuestionImpl qImpl,User currentUser)
    {

        System.out.println("Received question : "+ qImpl.toString());

        Question question = new Question (qImpl.getTheme(), qImpl.getLibelle(),
                                          qImpl.getPoids(), niveauRepository.findById(qImpl.getIdNiveau()).orElseThrow());
        Set<ReponseQuestion> reponseQuestions = new HashSet<>(qImpl.getReponsesQuestions());
        question.setQuestionCompetences(new HashSet<>());
        question.setReponsesQuestions(new HashSet<>());

        //Association competence-quiz
        Set<Competence> competences = new HashSet<>();
        for (Long cId : qImpl.getQuestionCompetences()) {
            competences.add(this.competenceRepository.findById(cId).orElseThrow());
        }

        // Ajout de l'entity à la bdd
        Question createdQuestion = this.questionRepository.save(question);
        createdQuestion.getQuestionCompetences().addAll(competences);

        // Creation des reponses associées à la question

        Set<ReponseQuestion> createdRepQuestions = new HashSet<>();
        for (ReponseQuestion rep: reponseQuestions) {
            rep.setQuestion(createdQuestion);
            ReponseQuestion newRep = this.repQuestionRepository.save(rep);
            createdRepQuestions.add(newRep);
        }
        System.out.println("CreatedResponse : "+createdRepQuestions);
        createdQuestion.getReponsesQuestions().addAll(createdRepQuestions);

        createdQuestion.setUser(currentUser);
        currentUser.getMyCreatedQuestion().add(createdQuestion);

        System.out.println("createdQuestion : "+createdQuestion.toString());

        return new ResponseEntity<>(this.questionRepository.saveAndFlush(createdQuestion), HttpStatus.OK);
    }

    public ResponseEntity<Question> updateQuestion(QuestionImpl qImpl)
    {
        System.out.println("Received question : "+ qImpl.toString());

        Question question = new Question (qImpl.getTheme(), qImpl.getLibelle(), qImpl.getPoids(), niveauRepository.findById(qImpl.getIdNiveau()).orElseThrow());
        //Set<ReponseQuestion> reponseQuestions = new HashSet<>(qImpl.getReponsesQuestions());
        question.setIdQuestion(qImpl.getIdQuestion());
        question.setQuestionCompetences(new HashSet<>());
        question.setReponsesQuestions(new HashSet<>());
        question.setListQuiz(new HashSet<>());

        qImpl.getQuestionCompetences().forEach(cId -> {
            System.out.println("competenceId : "+cId+" find : "+this.competenceRepository.existsById(cId));
            question.getQuestionCompetences().add(this.competenceRepository.findById(cId).orElse(null));
        });

        qImpl.getListQuiz().forEach(qId -> question.getListQuiz().add(this.quizRepository.findById(qId).orElse(null)));

        qImpl.getReponsesQuestions().forEach(rep -> {
            rep.setQuestion(this.questionRepository.findById(qImpl.getIdQuestion()).orElseThrow());
            ReponseQuestion repUdate = this.repQuestionRepository.saveAndFlush(rep);
            question.getReponsesQuestions().add(this.repQuestionRepository.findById(repUdate.getIdReponse()).orElse(null));
        });

        System.out.println("reponse : "+question.getReponsesQuestions());

        //upadate competences
        //Question questionUpdate = new Question();
        if(this.questionRepository.findById(qImpl.getIdQuestion()).isPresent())
        {
            this.setQuestionCompetences(qImpl.getIdQuestion(),question.getQuestionCompetences());
            this.setListQuiz(qImpl.getIdQuestion(),question.getListQuiz());
            //this.setReponsesQuestions(qImpl.getIdQuestion(),question.getReponsesQuestions());
            //questionUpdate=this.questionRepository.save(question);
            return new ResponseEntity<>(this.questionRepository.saveAndFlush(question), HttpStatus.OK);
        }
        else return new ResponseEntity<>(this.questionRepository.saveAndFlush(question), HttpStatus.OK);
    }

    public void setReponsesQuestions (Long questionId,Set<ReponseQuestion> reponses)
    {
        Set<ReponseQuestion> putReponses= new HashSet<>();
        reponses.forEach(rep -> putReponses.add(this.repQuestionRepository.saveAndFlush(rep)));

        Question putQuestion=this.questionRepository.findById(questionId).orElseThrow();
        putQuestion.setReponsesQuestions(putReponses);

        System.out.println("putReponses : "+putReponses);
//        putReponses.forEach(rep -> {
//            rep.setQuestion(putQuestion);
//            this.repQuestionRepository.saveAndFlush(rep);
//        });

        this.questionRepository.saveAndFlush(putQuestion);
    }

    public void setQuestionCompetences (Long questionId,Set<Competence> competences)
    {
        Set<Competence> putCompetences= new HashSet<>();
        competences.forEach(c-> putCompetences.add(this.competenceRepository.findById(c.getId()).orElseThrow()));

        Question putQuestion=this.questionRepository.findById(questionId).orElseThrow();
        putQuestion.setQuestionCompetences(putCompetences);

        putCompetences.forEach(competence -> {
            competence.getListQuestions().add(putQuestion);
            this.competenceRepository.saveAndFlush(competence);
        });

        this.questionRepository.saveAndFlush(putQuestion);
    }

    public void setListQuiz (Long questionId,Set<Quiz> quizz)
    {
        Set<Quiz> putQuizz= new HashSet<>();
        quizz.forEach(q-> putQuizz.add(this.quizRepository.findById(q.getIdQuiz()).orElseThrow()));

        Question putQuestion=this.questionRepository.findById(questionId).orElseThrow();
        putQuestion.setListQuiz(putQuizz);

        putQuizz.forEach(quiz -> {
            quiz.getQuizQuestions().add(putQuestion);
            this.quizRepository.saveAndFlush(quiz);
        });

        this.questionRepository.saveAndFlush(putQuestion);
    }

    public Question getQuestion(Long qId)
    {
        return this.questionRepository.findById(qId).orElseThrow(NullPointerException::new);
    }

    public List<Question> getAllQuestions()
    {
        return this.questionRepository.findAll();
    }

    public Set<Question> getAllQuestionsByUser(Long userId) {

        return questionRepository.findAll()
                .stream()
                .filter(question -> question.getUser().equals(userRepository.findById(userId).orElseThrow()))
                .collect(Collectors.toSet());
    }

    public void deleteQuestion(Long qId)
    {
        Question deletedQuestion = this.questionRepository.findById(qId).orElseThrow();
        if(this.questionRepository.findById(qId).isPresent()) {
            Set<ReponseQuestion> reponses = deletedQuestion.getReponsesQuestions();
            Set<Competence> competences = deletedQuestion.getQuestionCompetences();
            Set<Quiz> quizz = deletedQuestion.getListQuiz();

            quizz.forEach(quiz -> {
                this.quizRepository.findById(quiz.getIdQuiz()).orElseThrow().getQuizQuestions().remove(deletedQuestion);
                this.quizRepository.saveAndFlush(quiz);
            });

            competences.forEach(competence -> {
                this.competenceRepository.findById(competence.getId()).orElseThrow().getListQuiz().remove(deletedQuestion);
                this.competenceRepository.saveAndFlush(competence);
            });

            reponses.clear();
            competences.clear();

            quizz.clear();

            this.questionRepository.saveAndFlush(deletedQuestion);

            List<ReponseQuestion> reponseWithQuestionNull = this.repQuestionRepository
                                                                .findAll()
                                                                .stream()
                                                                .filter(rep-> rep.getQuestion() == null)
                                                                .collect(Collectors.toList());
            this.repQuestionRepository.deleteAll(reponseWithQuestionNull);

            deletedQuestion.setNiveau(null);
            this.userRepository.findById(deletedQuestion.getUser().getId()).orElseThrow().getMyCreatedQuestion().remove(deletedQuestion);
            deletedQuestion.setUser(null);

            this.questionRepository.deleteById(qId);
        }
    }

    public void deleteAllQuestions()
    {
        this.questionRepository.findAll().forEach(question -> this.deleteQuestion(question.getIdQuestion()));
    }

    public void addResponseToQuestion(Long repId, Long qId) { }

    public void deleteResponseFromQuestion(Long repId, Long qId) { }

    public Set<ReponseQuestion> getAllQuestionResponse(Long qId)
    {
        return this.questionRepository.findById(qId).orElseThrow().getReponsesQuestions();
    }

    public Set<ReponseQuestion> getAllCorrectQuestionResponse(Long qId)
    {
            return this.getAllQuestionResponse(qId).stream().filter(ReponseQuestion::getIsCorrect).collect(Collectors.toSet());
    }

    public long getQuestionPoids(Long qId) {

        return this.questionRepository.findById(qId).orElseThrow().getPoids();
    }
}
