def issue = get("/rest/api/2/issue/${issue.id}").asObject(Map).body //Запрос Issue
def originalEstimate = issue.fields.timeoriginalestimate //Original Estimate [секунды]
def trackedOriginalEstimate = issue.fields.customfield_10082 // Tracked Original Estimate [минуты]. Если Original Estimate изменилось, то до выполнения кода в Listener, хранит предыдущее значение Original Estimate.

def storyPoints = issue.fields.customfield_10028 //Story Points [число]
def trackedStoryPoints = issue.fields.customfield_10083 //Tracked Story Points [число]. Если Story Points изменилось, то до выполнения кода в Listener, хранит предыдущее значение Story Points.
def modelStoryPoint = issue.fields.customfield_10081 // - Model Story Point [минуты]

def minutesbalance = 0 // Определения четверти [минуты]

if (originalEstimate != null)
{
  originalEstimate = originalEstimate/60 //Конвертация Original Estimate в минуты

  if (originalEstimate == trackedOriginalEstimate && storyPoints == trackedStoryPoints)
  {
    return
  }

//Если Original Estimate был изменен
  if (originalEstimate != trackedOriginalEstimate && storyPoints != null)
  {
    originalEstimate = originalEstimate + (storyPoints * modelStoryPoint) //Включение актуальных рисков в Original Estimate
  }

//Если Story Points был изменен
  if (storyPoints != trackedStoryPoints && originalEstimate != null)
  {
    if (trackedStoryPoints == null) //Если предыдущее значение Story Points равно null
    {
      originalEstimate = originalEstimate + (storyPoints * modelStoryPoint) //Включение актуальных рисков в Original Estimate
    }
    else if (trackedStoryPoints != null)
    {
      originalEstimate = originalEstimate - (trackedStoryPoints * modelStoryPoint) //Извлечение первичного Original Estimate без рисков
      originalEstimate = originalEstimate + (storyPoints * modelStoryPoint) //Включение актуальных рисков в Original Estimate
    }
  }

//Округление Original Estimate к целым часам или получасам
  minutesbalance = originalEstimate-(Math.floor(originalEstimate/60)*60)

  if ((minutesbalance == 0) || (minutesbalance > 0 && minutesbalance <= 15))
  {
    originalEstimate = (Math.floor(originalEstimate/60))*60
  }
  else if ((minutesbalance > 15 && minutesbalance <= 30) || (minutesbalance > 30 && minutesbalance <= 45))
  {
    originalEstimate = ((Math.floor(originalEstimate/60))*60)+30
  }
  else if (minutesbalance > 45 && minutesbalance < 60)
  {
    originalEstimate = ((Math.floor(originalEstimate/60))*60)+60
  }

  put("/rest/api/2/issue/${issue.id}")
          .header("Content-Type", "application/json")
          .body([fields:[customfield_10083:storyPoints, customfield_10082:originalEstimate]]).asObject(Map).body
}
