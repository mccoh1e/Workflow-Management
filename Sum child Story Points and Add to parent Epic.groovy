def storyPointsField = get("/rest/api/2/field").asObject(List).body.find {(it as Map).name == 'Story Points'}.id //Получение ID поля Story Points
def epicLinkField = get("/rest/api/2/field").asObject(List).body.find {(it as Map).name == 'Epic Link'}.id //Получение ID поля Epic Link

def epicKey = issue.fields."${epicLinkField}" //Получение всех сущностей у Epic
def issuesInEpic = get("/rest/agile/1.0/epic/${epicKey}/issue")
        .asObject(Map)
        .body
        .issues as List<Map>
logger.info("Total issues in Epic for ${epicKey}: ${issuesInEpic.size()}")

def estimate = issuesInEpic.collect { Map issueInEpic -> //Сумма всех Estimate
  issueInEpic.fields."${storyPointsField}" ?: 0
}.sum()
logger.info("Summed estimate: ${estimate}")

def result = put("/rest/api/2/issue/${epicKey}") //Сеттинг Estimate в Epic
        .queryString("overrideScreenSecurity", Boolean.TRUE)
        .header('Content-Type', 'application/json')
        .body([
                fields: [
                        "${storyPointsField}": estimate
                ]
        ])
        .asString()

assert result.status >= 200 && result.status < 300 // Проверка статуса