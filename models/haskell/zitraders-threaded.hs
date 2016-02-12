import System.Random
import System.IO.Unsafe
import Data.Maybe
import qualified Data.Map as Map
import Control.Exception
import Data.Time
import Control.Parallel (par, pseq)
import Data.List (genericLength)
import Control.Exception (evaluate)

data Trader = Trader { ident :: Int,
                   price :: Double} deriving (Show, Eq)

{--
==========================================================================
PARAMETERS
==========================================================================
--}

minimum_price = 1.0
maximum_price = 30.0

maxTradersPerGroup = 0
maxAttemptsPerTrader = 100

numruns = 1

{--
==========================================================================
Helper functions
==========================================================================
--}

average :: RealFloat a => [a] -> a
average l = sum l / n
  where n = genericLength l

sd :: RealFloat a => [a] -> a
sd l = sqrt $ sum (map ((^2) . subtract mean) l) / n
  where n = genericLength l
        mean = sum l / n

rnlist :: Int -> IO [Double]
rnlist n = do
  mapM (\_ -> randomRIO (0.0,1.0)) [1..n]
  
randomfn :: Int -> [Double]
randomfn 0 = []
randomfn n = unsafePerformIO $ rnlist n

randomf1 :: Double
randomf1 = head $ unsafePerformIO $ rnlist 1

initTraders :: Int -> Double -> Double -> [Trader]
initTraders n minprice maxprice =
  let prices = map (\r -> minprice + r*(maxprice-minprice)) $ randomfn n
      makeTraders [] [] = []
      makeTraders (new_price:rest) (id:ids) = 
         (Trader {ident = id,
                price = new_price}) : makeTraders rest ids
  in
    makeTraders prices [1..n]


splitAndMap :: [Trader] -> Int -> [Map.Map Int Trader]
splitAndMap traders i = 
    let 
        (t1, t2) = splitAt ((length traders +1) `div` 2) traders
        do1 = splitAndMap t1 (i-1)
        do2 = splitAndMap t2 (i-1)
    in case i of
        0 -> [foldl (\map t -> Map.insert (ident t) t map) Map.empty traders]
        _ -> do1 `par` (do2 `pseq` (do1 ++ do2))

splitNAndMap :: [Trader] -> Int -> [Map.Map Int Trader]
splitNAndMap traders n = 
    let 
        (t1, t2) = splitAt ((length traders +1) `div` 2) traders
        do1 = splitNAndMap t1 n
        do2 = splitNAndMap t2 n
    in case (length traders < n+1) of
        True -> [foldl (\map t -> Map.insert (ident t) t map) Map.empty traders]
        False -> do1 `par` (do2 `pseq` (do1 ++ do2))  

        
{--
==========================================================================
Trade Functions
==========================================================================
--}

negotiatePrice :: Trader -> Trader -> Double
negotiatePrice seller buyer =
    let p1 = price seller
        p2 = price buyer
    in
        p1 + (p2 - p1)*(randomf1)
        
findBuyerFor :: Trader -> Map.Map Int Trader -> Int -> Maybe Trader
findBuyerFor s buyers i
    | i==Map.size buyers = Nothing
    | i==maxAttemptsPerTrader = Nothing
    | otherwise =
        let b = snd $ Map.elemAt i buyers
        in case ((price b) > price s) of
            True -> Just b
            False -> findBuyerFor s buyers (i+1)
    
        
doTrades :: Map.Map Int Trader -> Map.Map Int Trader -> [Double]
doTrades sellers buyers 
    | null sellers = []
    | null buyers = []
    | otherwise = 
        let s = snd $ Map.elemAt 0 sellers
            b = findBuyerFor s buyers 0
            trade s' b' = negotiatePrice s' b'
            del v inmap = Map.delete (ident v) inmap 
        in
            case (isNothing b) of
                True -> doTrades (del s sellers) (buyers)
                False -> (trade s b'):doTrades (del s sellers) (del b' buyers)
                    where b' = fromJust b
 

groupTrade :: [Map.Map Int Trader] -> [Map.Map Int Trader] -> [Double]
groupTrade _ [] = []
groupTrade [] _ = []
groupTrade (sm0:sms) (bm0:bms) = 
    doHead `par` (doRest `pseq` (doHead ++ doRest))
    where
        doHead = doTrades sm0 bm0
        doRest = groupTrade sms bms        

doParallelTrades :: [Trader] -> [Trader] -> Int -> [Double]
doParallelTrades sellers buyers i 
    | length buyers < 10 = []
    | otherwise =
        let 
            smaps = 
                case i<=0 of
                    True -> splitAndMap sellers (abs i)
                    False -> splitNAndMap sellers i
            bmaps = 
                case i<=0 of
                    True -> splitAndMap buyers (abs i)
                    False -> splitNAndMap buyers i
        in
            groupTrade smaps bmaps


simulate :: Int -> Int -> [Double]
simulate numbuyers groupsize =
    let buyers = initTraders numbuyers minimum_price maximum_price
        sellers = initTraders numbuyers minimum_price maximum_price
        prices = doParallelTrades sellers buyers groupsize
    in prices

  
runNtimes :: Int -> Int -> Int -> IO [(Double, Int, Double)]
runNtimes numbuyers groupsize i
    | i==0 = return ([])
    | otherwise = do
        let prices = simulate numbuyers groupsize
        start <- getCurrentTime
        av <- evaluate $ average prices
        end <- getCurrentTime
        dtime <- evaluate (realToFrac( diffUTCTime end start))
        rs <- runNtimes numbuyers groupsize (i-1)
        return ((av, length prices, dtime):rs)

experimentGroupSizes :: Int -> Int -> Int -> IO () --(Double, Int, Double)
experimentGroupSizes numbuyers i depth
    | i < (-depth) = return ()
    | otherwise = do
        values <- runNtimes numbuyers i numruns
        let first (x,_,_) = x
            second (_,x,_) = x
            third (_,_,x) = x
            prices = map (first) values
            trades = map (fromIntegral . second) values
            times = map (third) values
            avg_price = average prices
            avg_trades = average $ trades
            avg_time = average times
            sd_price = sd prices
            sd_trades = sd trades
            sd_time = sd times
        print (avg_time, sd_time)
        experimentGroupSizes numbuyers (i-1) depth

{--
MAIN
--}

getNum :: IO Int
getNum = readLn

main :: IO ()
main = do
  let prices n groupsize = simulate n groupsize
  putStr "How many buyers?"
  n <- getNum
  putStr "To what depth?"
  d <- getNum
  experimentGroupSizes n 0 d
    