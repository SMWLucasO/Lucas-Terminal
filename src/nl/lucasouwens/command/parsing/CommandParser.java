package nl.lucasouwens.command.parsing;

import nl.lucasouwens.logger.Logger;
import nl.lucasouwens.logger.MessageType;
import nl.lucasouwens.util.ArrayUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandParser {

    private static CommandParser instance = null;

    /**
     * Get access to the CommandParser singleton class
     * @return CommandParser
     */
    public static CommandParser getInstance() {
        if (instance == null) {
            instance = new CommandParser();
        }

        return instance;
    }

    /**
     * Parse a new command to see if it is applicable to be added to the register
     * if it is, add it.
     * @param command Command The command object of the command you are trying to register
     * @param method The method which executes your command
     * @return boolean True if it was added, otherwise false.
     */
    public boolean parse(Command command, Method method) {
        if (CommandRegister.getInstance().getRegister().containsKey(command)) {
            return true;
        } else {
            boolean hasCommandType = false;
            for (Annotation methodAnnotation : method.getAnnotations()) {
                if (methodAnnotation instanceof CommandType) {
                    hasCommandType = true;
                    break;
                }
            }

            if (hasCommandType) {
                CommandRegister.getInstance().getRegister().put(command, method);
                return true;
            }
        }
        return false;
    }

    /**
     * Execute a command in the terminal.
     * @param command string The command which you are trying to execute (with arguments and all)
     * @return boolean True if the command was executed, otherwise false.
     */
    public boolean execute(String command) {
        String[] commandPieces = command.split(" ");
        if(commandPieces.length >= 1) {
            Command commandToExecute = CommandRegister.getCommandByName(commandPieces[0]);
            if(!(commandToExecute == null)) {
                Method method = CommandRegister.getInstance().getRegister().get(CommandRegister.getCommandByName(commandPieces[0]));
                if (method != null) {
                    try {
                        String[] arguments = Arrays.copyOfRange(commandPieces, 1, commandPieces.length - 1 <= method.getParameterCount() ? commandPieces.length : method.getParameterCount() + 1);
                        if (arguments.length >= commandToExecute.getMinCommandSize()) {
                            arguments = this.enquotedToSingle(commandPieces);
                            arguments = this.fillEmptyPieces(method, arguments);
                            try {
                                if(arguments.length > method.getParameterCount()) {
                                    Logger.log("Unable to execute command, too many arguments", MessageType.ERROR);
                                } else {
                                    method.invoke(method.getDeclaringClass().newInstance(), arguments);
                                }
                            } catch (InstantiationException e) {
                                Logger.log("Failed to execute the specified command", MessageType.ERROR);
                            }

                            return true;
                        } else {
                            Logger.log(String.format("The command %s requires at least %d arguments.",
                                    commandToExecute.getCommand(), commandToExecute.getMinCommandSize()), MessageType.ERROR);
                        }
                    } catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
                        Logger.log("An error has occurred.", MessageType.ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }

        return false;
    }

    /**
     * Fill up the missing array pieces for the parameters (when using optional arguments)
     * @param method Method the method which you are filling the array for
     * @param commandPieces String[] The array which is to be filled up
     * @return commandPieces
     */
    private String[] fillEmptyPieces(Method method, String[] commandPieces) {
        if(commandPieces.length < method.getParameterCount()) {
            int leftover = method.getParameterCount() - commandPieces.length;
            if(leftover >= 1) {
                commandPieces = ArrayUtil.join(commandPieces, new String[leftover]);
            }
        }
        return commandPieces;
    }

    /**
     * Method to parse the command line arguments given correctly, useful for passing strings of text.
     * @param commandPieces String Arguments of the command
     * @return String[]
     */
    private String[] enquotedToSingle(String[] commandPieces) {
        String arguments = String.join(" ", Arrays.copyOfRange(commandPieces, 1, commandPieces.length));
        ArrayList<String> rebuiltArgumentArray = new ArrayList<>();
        for(String str : arguments.split("\"")) {
            rebuiltArgumentArray.add(str);
            arguments = arguments.replace(str, " ");

        }

        rebuiltArgumentArray.removeAll(Arrays.asList("", " ", null));
        return rebuiltArgumentArray.stream().toArray(String[]::new);
    }

}
