package br.edu.ifpb.analyserTitle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.edu.ifpb.analyserTitle.analysers.TitleAnalyzer;
import br.edu.ifpb.analyserTitle.stackExchangeAPI.entities.pojos.QuestionPojo;
import br.edu.ifpb.analyserTitle.stackExchangeAPI.entities.types.Answer;
import br.edu.ifpb.analyserTitle.stackExchangeAPI.entities.types.Comment;
import br.edu.ifpb.analyserTitle.stackExchangeAPI.entities.types.Question;


/**
 * 
 * <p>
 * 		<b> Gera resultados para dataset </b>
 * </p>
 *
 * <p>
 * Adicionar metadados do SO e gera resultados a partir
 * dos analizadores para Dataset.
 * </p>
 * 
 * 
 * @author <a href="https://github.com/FranckAJ">Franck Aragão</a>
 * @author <a href="https://github.com/JoseRafael97">Rafael Feitosa</a>

 *
 */
public class GenerateReults {
	
	/**
	 * 
	 */
	private TitleAnalyzer analyzer;
	

	
	
	/**
	 * 
	 */
	public GenerateReults() {
		this.analyzer = new TitleAnalyzer();
	}
	
	/**
	 * 
	 * @param questions
	 * @return
	 */
	public List<QuestionPojo> generateNotAnsweredQuestions(List<Question> questions, int questionAmount){
		
		List<QuestionPojo> questionPojos = new ArrayList<QuestionPojo>();
		
		for (Question question : questions) {
			if(question.getAnswerCount() <= 0){
				
				if(questionPojos.size() == questionAmount ){
					return questionPojos;
				}
				
				
				QuestionPojo qp = new QuestionPojo();
				
				/**
				 * metadados SO request to API
				 */
				qp.setColumnQuestion(question);
				
				/**
				 * metadados of time asks
				 */
				qp.setColumnDateBetwenQuestionComment(this.dateBetwenQuestionComment(question));
				qp.setColumnDateBetwenQuestionAnswer(this.dateBetwenQuestionAnswer(question));
				qp.setColumnDateBetwenCommentAnswer(this.dateBetwenCommentAnswer(question));
				
				/**
				 * Analizers of titles of questions
				 */
				qp.setColumnTotallyUpperCase(analyzer.isTotallyUpperCase(question.getTitle()));
				qp.setColumnParciallyUpperCase(analyzer.isPartiallyUpperCase(question.getTitle()));
				qp.setColumnSmallSizeTitle(analyzer.isSmallSizeTitle(question.getTitle()));
				qp.setColumnMediumSizeTitle(analyzer.isMediumSizeTitle(question.getTitle()));
				qp.setColumnContainsHelpOrUrgent(analyzer.containsHelpOrUrgent(question.getTitle()));
				
				questionPojos.add(qp);
			}
		}
		
		return questionPojos;
	}
	
	/**
	 * 
	 * @param questions
	 * @return
	 */
	public List<QuestionPojo> generateAnswedQuestions(List<Question> questions, int questionAmount){
		
		List<QuestionPojo> questionPojos = new ArrayList<QuestionPojo>();
		
		for (Question question : questions) {
			if(question.getAnswerCount() > 0 && question.getAnswers() != null){
				
				if(questionPojos.size() == questionAmount ){
					return questionPojos;
				}
				
				QuestionPojo qp = new QuestionPojo();
				
				/**
				 * metadados SO request to API
				 */
				qp.setColumnQuestion(question);
				
				/**
				 * metadados of time asks
				 */
				qp.setColumnDateBetwenQuestionComment(this.dateBetwenQuestionComment(question));
				qp.setColumnDateBetwenQuestionAnswer(this.dateBetwenQuestionAnswer(question));
				qp.setColumnDateBetwenCommentAnswer(this.dateBetwenCommentAnswer(question));
				
				/**
				 * Analizers of titles of questions
				 */
				qp.setColumnTotallyUpperCase(analyzer.isTotallyUpperCase(question.getTitle()));
				qp.setColumnParciallyUpperCase(analyzer.isPartiallyUpperCase(question.getTitle()));
				qp.setColumnSmallSizeTitle(analyzer.isSmallSizeTitle(question.getTitle()));
				qp.setColumnMediumSizeTitle(analyzer.isMediumSizeTitle(question.getTitle()));
				qp.setColumnContainsHelpOrUrgent(analyzer.containsHelpOrUrgent(question.getTitle()));
				
				questionPojos.add(qp);
			}
		}
		
		return questionPojos;
	}
	
	/**
	 * Parse list of comments of pojo @Comment to list String
	 * @param comments
	 * @return
	 */
	private List<String> parseComments(List<Comment> comments){
		List<String> bodys = new ArrayList<>();
		if(comments != null){
			for (Comment comment : comments) {
				bodys.add(comment.getBodyMarkdown());
			}
		}
		return bodys;
	}
	
	/**
	 * 
	 * @param question
	 * @return
	 */
	private Long dateBetwenQuestionComment(Question question){
		Long minutes = -1l;
		Comment comment = null;
		
		if(isCommented(question)){
			comment = question.getComments().get(0);
			
			minutes = this.minutesBetewn(question.getCreationDate(), comment.getCreationDate());
		}
		return minutes;
	}
	
	/**
	 * 
	 * @param question
	 * @return
	 */
	private Long dateBetwenQuestionAnswer(Question question){
		Long minutes = -1l;
		Answer answer = null;

		//question.getAnswers() != null add for merged questions example:
		//https://pt.stackoverflow.com/questions/179644/como-enviar-e-mail-em-c
		
		if(isAnwend(question) && question.getAnswers() != null){
			answer = question.getAnswers().get(0);
			
			minutes = this.minutesBetewn(question.getCreationDate(), answer.getCreationDate());
		}
		

		return minutes;
	}

	/**
	 * 
	 * @param question
	 * @return
	 */
	private Long dateBetwenCommentAnswer(Question question){
		Long minutes = -1l;
		
		
		
		if(isAnwend(question) && isCommented(question)){
			Answer answer = question.getAnswers().get(0);
			Comment comment = question.getComments().get(0);
			
			minutes = this.minutesBetewn(comment.getCreationDate(), answer.getCreationDate());
		}
		return minutes;
	}
	
	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	private Long minutesBetewn(Date date1, Date date2){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date1);
		
		Long minutes = (date2.getTime() - date1.getTime()) / (1000*60);
		
		return minutes;
	}
	
	/**
	 * 
	 * @param question
	 * @return
	 */
	private boolean isCommented(Question question){
		return question.getCommentCount() > 0; 
	}
	
	private boolean isAnwend(Question question){
		return question.isAnswered();
		
	}
}
