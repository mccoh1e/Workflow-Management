def storyPointsField = get("/rest/api/2/field").asObject(List).body.find {(it as Map).name == 'Story Points'}.id //Получение ID поля Story Points

def parentKey = issue.fields.parent.key //Получение всех сущностей типа Subtask у Родительской сущности
def allSubtasks = get("/rest/api/2/search")
        .queryString("jql", "parent=${parentKey}")
        .queryString("fields", "parent,${storyPointsField}")
        .asObject(Map)
        .body
        .issues as List<Map>
logger.info("Total subtasks for ${parentKey}: ${allSubtasks.size()}")

def estimate = allSubtasks.collect { Map subtask -> //Сумма всех Estimate
    subtask.fields."${storyPointsField}" ?: 0
}.sum()
logger.info("Summed estimate: ${estimate}")

def fields = get('/rest/api/2/field') //Получение ID полей
        .asObject(List)
        .body as List<Map>

def result = put("/rest/api/2/issue/${parentKey}") //Сеттинг Estimate в Родительскую сущность
        .header('Content-Type', 'application/json')
        .body([
                fields: [
                        (storyPointsField): estimate
                ]
        ])
        .asString()

assert result.status >= 200 && result.status < 300 //Проверка статуса