import Swift

var catSet = Set<String>()     

class TodoItem : Equatable  {
    var title : String
    var content : String
    var priority : Int
    var category : String?
    var createDate : Double
    init(title: String, content : String, priority : Int, category: String?, createDate:Double) {
      self.title = title
      self.content = content
      self.priority = priority
      self.category = category
      self.createDate = createDate
   }
}

func ==(lhs: TodoItem, rhs: TodoItem) -> Bool {
   return (lhs.title == rhs.title) && (lhs.content == rhs.content) && (lhs.priority == rhs.priority)
}

var todoList = [TodoItem]()


func printTodo(todoitem: TodoItem, number: Int) {
    print("number: \(number) , title: \(todoitem.title) , content: \(todoitem.content) , priority: \(todoitem.priority) , createDate: \(todoitem.createDate)", terminator: "")
    if todoitem.category != nil {
        print(", category: \(todoitem.category!)")
    } else {
        print("")
    }
}

func getAllTodos() {
    //print
    for i in 0 ..< todoList.count {
        printTodo(todoitem : todoList[i], number : i)
    }
}

func getCategory(category : String) {
    //print
    for i in 0 ..< todoList.count {
        if todoList[i].category == category{
            printTodo(todoitem : todoList[i], number: i)
        }
    }
}

func sort(order:String) {
    switch order {
        case "AscDate":
            todoList.sort{$0.createDate < $1.createDate}
        case "DescDate":
            todoList.sort{$0.createDate > $1.createDate}
        case "AscTitle":
            todoList.sort{$0.title < $1.title}
        case "DescTitle":
            todoList.sort{$0.title > $1.title}
        case "AscPriority":
            todoList.sort{$0.priority < $1.priority}
        case "DescPriority":
            todoList.sort{$0.priority > $1.priority}
        default:
            print("Invalid input")
    }
    getAllTodos()
}

func createTodo(title: String, content : String, priority : Int, category:String?, createDate:Double) {
    let td = TodoItem(title:title, content:content, priority:priority, category:category, createDate:createDate)
    todoList.append(td)
    if td.category != nil {
        catSet.insert(td.category!)
    }
}

func createCategory(category:String) {
    catSet.insert(category)
}

func addToCategory(number:Int, category:String) {
    todoList[number].category = category
}

func edit(number: Int, title: String, content : String, priority : Int) {
    todoList[number].title = title
    todoList[number].content = content
    todoList[number].priority = priority
}

func toInt(input:String) -> Int {
    let number = Int(input)!
    return number
}

func delete(number: Int) {
    todoList.remove(at: number)
}

func help() {
    print("case \"createItem\":\n" +
                "                var ctg = (instructionArr.count > 4 && !instructionArr[4].isEmpty)?instructionArr[4]:nil\n" +
                "                createTodo(title:instructionArr[1], content:instructionArr[2], priority:instructionArr[3], category:ctg, createDate:NSDate().timeIntervalSince1970)\n" +
                "            case \"getAll\":\n" +
                "                getAllTodos()\n" +
                "            case \"getCategory\":\n" +
                "                getCategory(category:instructionArr[1])\n" +
                "            case \"edit\":\n" +
                "                edit(number:instructionArr[1], title:instructionArr[2], content:instructionArr[3], priority:instructionArr[4])\n" +
                "            case \"delete\":\n" +
                "                delete(number:instructionArr[1])\n" +
                "            case \"createCategory\":\n" +
                "                createCategory(category:instructionArr[1])\n" +
                "            case \"addToCategory\":\n" +
                "                addToCategory(number:instructionArr[1], category:instructionArr[2])\n" +
                "            case \"exit\":\n" +
                "                exit(-1)");
    print("please do a choise!\n#################")
}


func parseInput() {
    while let line = readLine() {
        let instructionArr:[String] = line.components(separatedBy: "$$")
        let command:String = instructionArr[0] 
        switch command {
            case "createItem":
                let ctg = (instructionArr.count > 4 && !instructionArr[4].isEmpty) ? instructionArr[4] : nil
                createTodo(title:instructionArr[1], content:instructionArr[2], priority:toInt(input:instructionArr[3]), category:ctg, createDate:NSDate().timeIntervalSince1970)
            case "getAll":
                getAllTodos()
            case "getCategory":
                getCategory(category:instructionArr[1])
            case "edit":
                edit(number:toInt(input:instructionArr[1]), title:instructionArr[2], content:instructionArr[3], priority:toInt(input:instructionArr[4]))
            case "delete":
                delete(number:toInt(input:instructionArr[1]))
            case "createCategory":
                createCategory(category:instructionArr[1])
            case "addToCategory":
                addToCategory(number:toInt(input:instructionArr[1]), category:instructionArr[2])
            case "sort":
                sort(order:instructionArr[1])
            case "exit":
                exit(-1)
            case "help":
                help()
            default:
                print("Invalid input")
        }
    }
    print("##################")
}

help()
parseInput()