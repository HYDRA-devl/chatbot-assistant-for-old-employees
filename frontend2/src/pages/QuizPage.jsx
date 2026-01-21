import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { quizAPI } from '../services/api';

export default function QuizPage() {
  const { quizId } = useParams();
  const navigate = useNavigate();
  const [quiz, setQuiz] = React.useState(null);
  const [questions, setQuestions] = React.useState([]);
  const [answers, setAnswers] = React.useState({});
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [result, setResult] = React.useState(null);
  const [error, setError] = React.useState('');

  const user = React.useMemo(() => {
    const raw = localStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
  }, []);

  React.useEffect(() => {
    loadQuiz();
  }, [quizId]);

  const loadQuiz = async () => {
    try {
      const [quizData, questionsData] = await Promise.all([
        quizAPI.getQuiz(quizId),
        quizAPI.getQuizQuestions(quizId)
      ]);
      setQuiz(quizData);
      setQuestions(questionsData);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerChange = (questionId, answer) => {
    setAnswers(prev => ({ ...prev, [questionId]: answer }));
  };

  const handleSubmit = async () => {
    // Check if all questions are answered
    if (Object.keys(answers).length !== questions.length) {
      alert('Please answer all questions before submitting');
      return;
    }

    setSubmitting(true);
    try {
      const attempt = await quizAPI.submitQuiz(quizId, user.id, answers);
      setResult(attempt);
      
      // Update user points in localStorage
      const updatedUser = { ...user, totalPoints: user.totalPoints + attempt.pointsEarned };
      localStorage.setItem('currentUser', JSON.stringify(updatedUser));
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-gray-500">Loading quiz...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center">
          <div className="text-red-600 mb-4">{error}</div>
          <button onClick={() => navigate('/chat')} className="btn-primary">
            Back to Chat
          </button>
        </div>
      </div>
    );
  }

  // Show results
  if (result) {
    const percentage = Math.round((result.score / result.totalQuestions) * 100);
    const passed = percentage >= 70;

    return (
      <div className="h-full overflow-y-auto p-6">
        <div className="max-w-3xl mx-auto">
          <div className="card">
            <div className="card-body text-center">
              <div className="text-6xl mb-4">
                {passed ? '🎉' : '📚'}
              </div>
              <h2 className="text-3xl font-bold text-gray-900 mb-2">
                {passed ? 'Great Job!' : 'Keep Learning!'}
              </h2>
              <p className="text-gray-600 mb-6">
                You scored {result.score} out of {result.totalQuestions} questions correctly
              </p>

              <div className="flex items-center justify-center gap-8 mb-6">
                <div className="text-center">
                  <div className="text-4xl font-bold text-blue-600">{percentage}%</div>
                  <div className="text-sm text-gray-600">Score</div>
                </div>
                <div className="text-center">
                  <div className="text-4xl font-bold text-green-600">+{result.pointsEarned}</div>
                  <div className="text-sm text-gray-600">Points Earned</div>
                </div>
              </div>

              {/* Show correct answers */}
              <div className="text-left mt-8 space-y-4">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Review Answers</h3>
                {questions.map((q, idx) => {
                  const userAnswer = answers[q.id];
                  const isCorrect = userAnswer === q.correctAnswer;
                  
                  return (
                    <div key={q.id} className={`p-4 rounded-lg border-2 ${
                      isCorrect ? 'bg-green-50 border-green-300' : 'bg-red-50 border-red-300'
                    }`}>
                      <div className="flex items-start gap-3">
                        <div className="text-2xl flex-shrink-0">
                          {isCorrect ? '✓' : '✗'}
                        </div>
                        <div className="flex-1">
                          <div className="font-medium text-gray-900 mb-2">
                            {idx + 1}. {q.questionText}
                          </div>
                          <div className="text-sm space-y-1">
                            <div className={userAnswer === 'A' ? 'font-semibold' : ''}>
                              A: {q.optionA} {q.correctAnswer === 'A' && '✓'}
                            </div>
                            <div className={userAnswer === 'B' ? 'font-semibold' : ''}>
                              B: {q.optionB} {q.correctAnswer === 'B' && '✓'}
                            </div>
                            <div className={userAnswer === 'C' ? 'font-semibold' : ''}>
                              C: {q.optionC} {q.correctAnswer === 'C' && '✓'}
                            </div>
                            <div className={userAnswer === 'D' ? 'font-semibold' : ''}>
                              D: {q.optionD} {q.correctAnswer === 'D' && '✓'}
                            </div>
                          </div>
                          {q.explanation && (
                            <div className="mt-2 text-sm text-gray-700 bg-white bg-opacity-50 p-2 rounded">
                              💡 {q.explanation}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="mt-8 flex gap-3 justify-center">
                <button
                  onClick={() => navigate('/chat')}
                  className="btn-primary"
                >
                  Back to Chat
                </button>
                <button
                  onClick={() => navigate('/dashboard')}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                >
                  Dashboard
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Show quiz questions
  return (
    <div className="h-full overflow-y-auto p-6">
      <div className="max-w-3xl mx-auto">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Quiz: {quiz.topic}</h2>
          <p className="text-gray-600 mt-1">
            Answer all {questions.length} questions to earn bonus points!
          </p>
        </div>

        <div className="space-y-6">
          {questions.map((question, idx) => (
            <div key={question.id} className="card">
              <div className="card-body">
                <div className="flex items-start gap-3 mb-4">
                  <div className="flex-shrink-0 w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center font-semibold">
                    {idx + 1}
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-medium text-gray-900">
                      {question.questionText}
                    </h3>
                  </div>
                </div>

                <div className="space-y-2 ml-11">
                  {['A', 'B', 'C', 'D'].map(option => (
                    <label
                      key={option}
                      className={`flex items-center p-3 rounded-lg border-2 cursor-pointer transition-all ${
                        answers[question.id] === option
                          ? 'bg-blue-50 border-blue-500'
                          : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      <input
                        type="radio"
                        name={`question-${question.id}`}
                        value={option}
                        checked={answers[question.id] === option}
                        onChange={() => handleAnswerChange(question.id, option)}
                        className="mr-3"
                      />
                      <span className="font-medium mr-2">{option}:</span>
                      <span>{question[`option${option}`]}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-8 flex items-center justify-between bg-white border-t-2 border-gray-200 p-4 rounded-lg sticky bottom-0">
          <div className="text-sm text-gray-600">
            {Object.keys(answers).length} of {questions.length} answered
          </div>
          <div className="flex gap-3">
            <button
              onClick={() => navigate('/chat')}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              disabled={submitting || Object.keys(answers).length !== questions.length}
              className="btn-primary"
            >
              {submitting ? 'Submitting...' : 'Submit Quiz'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
