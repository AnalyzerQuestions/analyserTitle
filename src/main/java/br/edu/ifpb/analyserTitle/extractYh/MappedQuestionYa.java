
package br.edu.ifpb.analyserTitle.extractYh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import br.edu.ifpb.analyserTitle.entities.Question;
import br.edu.ifpb.analyserTitle.extractYh.enumarations.QuestionType;

/**
 * 
 * <p>
 * <b> {@link MappedQuestionYa} </b>
 * </p>
 *
 * <p>
 * mapped the questions in Yahho Answers.
 * </p>
 * 
 * @author <a href="https://github.com/JoseRafael97">Rafael Feitosa</a>
 */
public class MappedQuestionYa {

	private final String URL_PAGE = "https://br.answers.yahoo.com";

	private Map<String, String> mainCategoryList;

	private Document document;

	/**
	 * Default construct
	 */
	public MappedQuestionYa() {
		initListOfCategory();
	}

	/**
	 * Search questions not answered in the Yahoo Answers
	 * 
	 * @param numberQuestion
	 *            - number of question
	 * @return List of questions
	 */
	public List<Question> questionsNotAnswered(Integer numberQuestion) {
		return visitPages(QuestionType.NOT_ANSWERED, numberQuestion);
	}

	/**
	 * Search questions with answer in the Yahoo Answers
	 * 
	 * @param numberQuestion
	 * @return
	 */
	public List<Question> questionsAnswered(Integer numberQuestion) {
		return visitPages(QuestionType.ANSWER, numberQuestion);
	}

	/**
	 * Search questions with accepted answer in the Yahoo Answers
	 * 
	 * @param numberQuestion
	 * @return
	 */
	public List<Question> questionsWithAcceptedAnswer(Integer numberQuestion) {
		return visitPages(QuestionType.JUST_WITH_ANSWERED_ANSWER, numberQuestion);
	}

	/**
	 * Search questions in the Yahoo Answers
	 * 
	 * @param numberQuestion
	 * @return
	 */
	public List<Question> allQuestions(Integer numberQuestion) {
		return visitPages(null, numberQuestion);
	}

	/**
	 * Visit pages with category and question for extract information in the
	 * Yahoo Answers.
	 * 
	 * @param questionType
	 * @return
	 */
	public List<Question> visitPages(QuestionType questionType, Integer numberQuestion) {
		List<Question> questionsYahoo = new ArrayList<Question>();

		for (Map.Entry<String, String> entry : mainCategoryList.entrySet()) {

			try {

				Elements linkSubCategory = extractLinkSubCategoryYh(entry.getValue());

				for (int i = 0; i < linkSubCategory.size(); i++) {

					Elements questionsLinks = extractLinkQuestionYh(URL_PAGE + linkSubCategory.get(i).attr("href"));

					for (int j = 0; j < questionsLinks.size(); j++) {

						if (questionsYahoo.size() >= numberQuestion) {
							return questionsYahoo;
						}
						Question question = extractQuestionYh(URL_PAGE + questionsLinks.get(j).attr("href"));

						if (validateInsertQuestionInList(questionType, question)) {
							questionsYahoo.add(question);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return questionsYahoo;

	}

	/**
	 * Validate Filter of question type.
	 * 
	 * @param questionType
	 * @param question
	 * @return
	 */
	private boolean validateInsertQuestionInList(QuestionType questionType, Question question) {

		if (questionType == null) {
			return true;

		} else if (questionType.equals(QuestionType.ANSWER) && question.getAnswersNumber() > 0) {
			return true;

		} else if (questionType.equals(QuestionType.NOT_ANSWERED) && question.getAnswersNumber() == 0) {
			return true;

		} else if (questionType.equals(QuestionType.JUST_WITH_ANSWERED_ANSWER) && question.isAcceptedAnswers()) {
			return true;

		} else {
			return false;
		}
	}

	/**
	 * Extract link for access subcategories of the questions Politicas
	 * Socias->have subcategory->imigração.
	 * 
	 * @param link
	 *            - Link of the page what have link for subcategory
	 * @return
	 * @throws IOException
	 */
	private Elements extractLinkSubCategoryYh(String link) throws IOException {

		document = Jsoup.connect(link).get();

		Elements linkSubCategory = document.getElementsByClass("D-ib Clr-w Fz-13 Pt-8 W-32").select("[href]");

		return linkSubCategory;
	}

	/**
	 * Extract link of the questions in page with list of questions.
	 * 
	 * @param link
	 *            - link of page with list of question
	 * @return
	 * @throws IOException
	 */
	private Elements extractLinkQuestionYh(String link) throws IOException {

		document = Jsoup.connect(link).get();

		Elements questionsLinks = document.getElementsByClass("Fz-14 Fw-b Clr-b Wow-bw title").select("[href]");

		return questionsLinks;

	}

	/**
	 * Extract contenct of the question for Object of type Question with
	 * name,body, countAnswers e etc.
	 * 
	 * @param link
	 *            - Link of the question
	 * @return Question
	 * @throws IOException
	 */
	public Question extractQuestionYh(String link) throws IOException {

		if (link.contains("question") && link.contains("qid")) {

		
			
			document = Jsoup.connect(link).get();

			String title = document.getElementsByClass("Fz-24 Fw-300 Mb-10").text();
			String description = document.getElementsByClass("ya-q-text").text();
			String numberAnswers = document.getElementsByClass("Mend-10 Fz-13 Fw-n D-ib").text();
			Boolean acceptedAnswer = document.getElementsByClass("ya-ba-title Fw-b").text().equals("Melhor resposta:");
			Integer numberAnswersInt = 0;
			List<String> tags = extractTagsQuestionYh();
			
			int year = Integer.parseInt(link.substring(48,  52));
			int month =  Integer.parseInt(link.substring(52, 54));
			int day =  Integer.parseInt(link.substring(54,56));
			
			Calendar c = Calendar.getInstance();
			c.set(year, month, day, 0, 0); 

			if (numberAnswers.trim().length() > 0) {

				if (numberAnswers.trim().length() < 14) {
					numberAnswers = numberAnswers.substring(0, 2).trim();

				} else if (numberAnswers.trim().length() < 16) {
					numberAnswers = numberAnswers.substring(0, 3).trim();

				} else {
					numberAnswers = numberAnswers.substring(0, 4).trim();
				}

				numberAnswersInt = Integer.parseInt(numberAnswers);
			}

			return new Question(title, description, link, numberAnswersInt, acceptedAnswer, tags, c.getTime());

		}

		return null;
	}

	/**
	 * Extract tags of question page Yahoo Answers
	 * 
	 * @return
	 */
	public List<String> extractTagsQuestionYh() {
		List<String> tags = new ArrayList<>();

		Elements element = document.getElementsByClass("Clr-b").select("[title]");

		for (int i = 0; i < element.size(); i++) {
			tags.add(element.get(i).attr("title"));
		}

		return tags;
	}

	/**
	 * Initializes list of categories in Yahoo answers
	 */
	private void initListOfCategory() {
		mainCategoryList = new HashMap<String, String>();
		mainCategoryList.put("Eletrônicos", "https://br.answers.yahoo.com/dir/index?sid=396545014");
		mainCategoryList.put("Carros e Transportes", "https://br.answers.yahoo.com/dir/index?sid=396545311");
		mainCategoryList.put("Animais de Estimação", "https://br.answers.yahoo.com/dir/index?sid=396545443");
		mainCategoryList.put("Ciências e Matemática", "https://br.answers.yahoo.com/dir/index?sid=396545122");
		mainCategoryList.put("Ecologia e Meio Ambiente", "https://br.answers.yahoo.com/dir/index?sid=396545451");
		mainCategoryList.put("Entretenimento e Música", "https://br.answers.yahoo.com/dir/index?sid=396545016");
		mainCategoryList.put("Governo e Política", "https://br.answers.yahoo.com/dir/index?sid=396545444");
		mainCategoryList.put("Negócios Regionais", "https://br.answers.yahoo.com/dir/index?sid=396545401");
		mainCategoryList.put("Sociedade e Cultura", "https://br.answers.yahoo.com/dir/index?sid=396545454");
		mainCategoryList.put("Artes e Humanidades", "https://br.answers.yahoo.com/dir/index?sid=396545012");
		mainCategoryList.put("Casa e Jardim", "https://br.answers.yahoo.com/dir/index?sid=396545394");
		mainCategoryList.put("Comidas e Bebidas", "https://br.answers.yahoo.com/dir/index?sid=396545367");
		mainCategoryList.put("Educação e Referência", "https://br.answers.yahoo.com/dir/index?sid=396545015");
		mainCategoryList.put("Esportes", "https://br.answers.yahoo.com/dir/index?sid=396545213");
		mainCategoryList.put("Gravidez e Maternidade", "https://br.answers.yahoo.com/dir/index?sid=396546046");
		mainCategoryList.put("Negócios e Finanças", "https://br.answers.yahoo.com/dir/index?sid=396545013");
		mainCategoryList.put("Restaurantes", "https://br.answers.yahoo.com/dir/index?sid=396545327");
		mainCategoryList.put("Viagens", "https://br.answers.yahoo.com/dir/index?sid=396545469");
		mainCategoryList.put("Beleza e Estilo", "https://br.answers.yahoo.com/dir/index?sid=396545144");
		mainCategoryList.put("Ciências Sociais", "https://br.answers.yahoo.com/dir/index?sid=396545301");
		mainCategoryList.put("Computadores e Internet", "https://br.answers.yahoo.com/dir/index?sid=396545660");
		mainCategoryList.put("Família e Relacionamentos", "https://br.answers.yahoo.com/dir/index?sid=396545433");
		mainCategoryList.put("Jogos e Recreação", "https://br.answers.yahoo.com/dir/index?sid=396545019");
		mainCategoryList.put("Notícias e Eventos", "https://br.answers.yahoo.com/dir/index?sid=396545439");
		mainCategoryList.put("Saúde", "https://br.answers.yahoo.com/dir/index?sid=396545018");
	}

	public static void main(String[] args) throws IOException {
		MappedQuestionYa mappedQuestionYa = new MappedQuestionYa();

		System.out.println("NUMERO QUESTION: " + mappedQuestionYa.allQuestions(3).size());
	}
}
