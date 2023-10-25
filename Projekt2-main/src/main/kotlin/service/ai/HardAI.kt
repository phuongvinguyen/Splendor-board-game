package service.ai

/**
 * This AI is the strongest AI, which is based on [MediumAI].
 */
class HardAI : MediumAI() {
    override fun findLeafNode(root: MCSTreeNode): MCSTreeNode {
        var child: MCSTreeNode = root

        while (child.children.isNotEmpty()) {
            // Steven braucht diese Zeile
            // if(random.nextDouble(0.0, 100.0) in 0.0..(ucts.random().second / uctMax!!.second)*100)

            // TODONE: HardAI kann so nicht parallelisiert werden
            // ODER ETWA DOCH? DAM DAM DAM
            child = child.children.values.maxByOrNull { it.getUct(child.game.currentPlayer) }!!
        }
        return child
    }


    /**
     * This method returns the AI that should be used when simulating games
     *
     * @return The AI
     */
    override fun getPlayToEndAI() = RandomWeightedAI()
}